package com.music.JunStudio.upload.service;


import com.music.JunStudio.upload.domain.MediaFileEntity;
import com.music.JunStudio.upload.domain.UploadPartEntity;
import com.music.JunStudio.upload.domain.UploadSessionEntity;
import com.music.JunStudio.upload.dto.*;
import com.music.JunStudio.upload.repo.MediaFileRepository;
import com.music.JunStudio.upload.repo.UploadPartRepository;
import com.music.JunStudio.upload.repo.UploadSessionRepository;
import com.music.JunStudio.upload.security.UploadAuthHelper;
import com.music.JunStudio.upload.storage.StorageMultipartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final long MAX_SIZE = 20L * 1024 * 1024 * 1024;   // 20GB
    private static final long CHUNK_SIZE = 16L * 1024 * 1024;        // 16MB
    private static final Set<String> TERMINAL = Set.of("COMPLETED", "ABORTED", "EXPIRED");

    private final UploadSessionRepository uploadSessionRepository;
    private final UploadPartRepository uploadPartRepository;
    private final MediaFileRepository mediaFileRepository;
    private final StorageMultipartService storage;
    private final UploadAuthHelper uploadAuthHelper;

    @Override
    @Transactional
    public CreateUploadSessionResponse createSession(CreateUploadSessionRequest req) {
        if (req.fileSize() <= 0 || req.fileSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size must be between 1 byte and 20GB");
        }

        UUID sessionId = UUID.randomUUID();
        String safeFileName = req.fileName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = "uploads/" + Instant.now().toEpochMilli() + "-" + sessionId + "-" + safeFileName;
        int totalParts = (int) Math.ceil((double) req.fileSize() / CHUNK_SIZE);
        Instant now = Instant.now();
        Instant expires = now.plus(Duration.ofHours(24));

        var init = storage.initMultipart(objectKey, req.contentType());

        UploadSessionEntity s = new UploadSessionEntity();
        s.setId(sessionId);
        s.setUserId(uploadAuthHelper.currentUserIdOrThrow());
        s.setFileName(req.fileName());
        s.setFileSizeBytes(req.fileSize());
        s.setContentType(req.contentType());
        s.setObjectKey(objectKey);
        s.setProviderUploadId(init.uploadId());
        s.setChunkSizeBytes(CHUNK_SIZE);
        s.setTotalParts(totalParts);
        s.setStatus("INIT");
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        s.setExpiresAt(expires);
        uploadSessionRepository.save(s);

        return new CreateUploadSessionResponse(
                s.getId(),
                s.getObjectKey(),
                s.getProviderUploadId(),
                s.getChunkSizeBytes(),
                s.getTotalParts(),
                s.getExpiresAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UploadSessionStatusResponse getSessionStatus(UUID sessionId) {
        UploadSessionEntity s = mustGet(sessionId);

        List<Integer> uploaded = uploadPartRepository.findByUploadSessionIdOrderByPartNumberAsc(sessionId)
                .stream()
                .map(UploadPartEntity::getPartNumber)
                .toList();

        return new UploadSessionStatusResponse(
                s.getId(),
                s.getStatus(),
                s.getChunkSizeBytes(),
                s.getTotalParts(),
                uploaded
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PresignPartsResponse presignParts(UUID sessionId, List<Integer> partNumbers) {
        UploadSessionEntity s = mustGet(sessionId);
        validateActiveSession(s);

        List<Integer> normalized = partNumbers.stream().distinct().sorted().toList();
        for (Integer p : normalized) {
            validatePartNumber(p, s.getTotalParts());
        }

        List<PresignedPartUrl> urls = normalized.stream()
                .map(p -> new PresignedPartUrl(
                        p,
                        storage.presignUploadPartUrl(
                                s.getObjectKey(),
                                s.getProviderUploadId(),
                                p,
                                Duration.ofMinutes(15)
                        )
                ))
                .toList();

        return new PresignPartsResponse(sessionId, urls);
    }

    @Override
    @Transactional
    public void completeParts(UUID sessionId, List<CompletedPartRequest> parts) {
        UploadSessionEntity s = mustGet(sessionId);
        validateActiveSession(s);
        Instant now = Instant.now();

        for (CompletedPartRequest p : parts) {
            validatePartNumber(p.partNumber(), s.getTotalParts());

            UploadPartEntity.Pk pk = new UploadPartEntity.Pk();
            pk.setUploadSessionId(sessionId);
            pk.setPartNumber(p.partNumber());

            UploadPartEntity e = uploadPartRepository.findById(pk).orElseGet(UploadPartEntity::new);
            e.setUploadSessionId(sessionId);
            e.setPartNumber(p.partNumber());
            e.setEtag(p.etag());
            e.setChecksum(p.checksum());
            e.setSizeBytes(p.sizeBytes());
            e.setUploadedAt(now);

            uploadPartRepository.save(e); // idempotent upsert behavior
        }

        s.setStatus("UPLOADING");
        s.setUpdatedAt(now);
        uploadSessionRepository.save(s);
    }

    @Override
    @Transactional
    public FinalizeUploadResponse finalizeUpload(UUID sessionId) {
        UploadSessionEntity s = mustGet(sessionId);
        validateActiveSession(s);

        List<UploadPartEntity> saved = uploadPartRepository.findByUploadSessionIdOrderByPartNumberAsc(sessionId);

        if (saved.size() != s.getTotalParts()) {
            throw new IllegalStateException(
                    "Cannot finalize: missing parts. expected=" + s.getTotalParts() + ", actual=" + saved.size()
            );
        }

        Set<Integer> partNums = saved.stream()
                .map(UploadPartEntity::getPartNumber)
                .collect(Collectors.toSet());

        for (int i = 1; i <= s.getTotalParts(); i++) {
            if (!partNums.contains(i)) {
                throw new IllegalStateException("Cannot finalize: missing part " + i);
            }
        }

        Map<Integer, String> etagByPart = saved.stream()
                .collect(Collectors.toMap(
                        UploadPartEntity::getPartNumber,
                        UploadPartEntity::getEtag,
                        (a, b) -> b
                ));

        List<StorageMultipartService.StoragePart> completeParts = etagByPart.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new StorageMultipartService.StoragePart(e.getKey(), e.getValue()))
                .toList();

        storage.completeMultipart(s.getObjectKey(), s.getProviderUploadId(), completeParts);

        s.setStatus("COMPLETED");
        s.setUpdatedAt(Instant.now());
        uploadSessionRepository.save(s);

        MediaFileEntity mf = new MediaFileEntity();
        mf.setUserId(s.getUserId());
        mf.setObjectKey(s.getObjectKey());
        mf.setFileName(s.getFileName());
        mf.setContentType(s.getContentType());
        mf.setSizeBytes(s.getFileSizeBytes());
        mf.setUploadSessionId(s.getId());
        mf.setProcessingStatus("PENDING");
        mf.setCreatedAt(Instant.now());
        mf.setUpdatedAt(Instant.now());
        mediaFileRepository.save(mf);

        return new FinalizeUploadResponse(mf.getId(), mf.getProcessingStatus());
    }

    @Override
    @Transactional
    public void abortUpload(UUID sessionId) {
        UploadSessionEntity s = mustGet(sessionId);

        if (!"COMPLETED".equals(s.getStatus())) {
            storage.abortMultipart(s.getObjectKey(), s.getProviderUploadId());
        }

        s.setStatus("ABORTED");
        s.setUpdatedAt(Instant.now());
        uploadSessionRepository.save(s);
    }

    private UploadSessionEntity mustGet(UUID id) {
        return uploadSessionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Upload session not found: " + id));
    }

    private void validatePartNumber(int partNumber, int totalParts) {
        if (partNumber < 1 || partNumber > totalParts) {
            throw new IllegalArgumentException("Invalid part number: " + partNumber);
        }
    }

    private void validateActiveSession(UploadSessionEntity s) {
        if (Instant.now().isAfter(s.getExpiresAt())) {
            throw new IllegalStateException("Upload session expired");
        }
        if (TERMINAL.contains(s.getStatus())) {
            throw new IllegalStateException("Upload session not active: " + s.getStatus());
        }
    }
}