package com.music.JunStudio.upload.scheduler;


import com.music.JunStudio.upload.domain.UploadSessionEntity;
import com.music.JunStudio.upload.repo.UploadSessionRepository;
import com.music.JunStudio.upload.storage.StorageMultipartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadCleanupScheduler {

    private static final Set<String> TERMINAL = Set.of("COMPLETED", "ABORTED", "EXPIRED");

    private final UploadSessionRepository uploadSessionRepository;
    private final StorageMultipartService storageMultipartService;

    /**
     * Every hour:
     * - finds expired non-terminal upload sessions
     * - aborts multipart upload in object storage
     * - marks session EXPIRED
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireAndAbortStaleUploads() {
        Instant now = Instant.now();

        List<UploadSessionEntity> candidates = uploadSessionRepository.findAll().stream()
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isBefore(now))
                .filter(s -> !TERMINAL.contains(s.getStatus()))
                .toList();

        if (candidates.isEmpty()) {
            log.debug("Upload cleanup: no expired active sessions found");
            return;
        }

        log.info("Upload cleanup: found {} expired active sessions", candidates.size());

        for (UploadSessionEntity s : candidates) {
            try {
                storageMultipartService.abortMultipart(s.getObjectKey(), s.getProviderUploadId());
            } catch (Exception ex) {
                // Continue cleanup even if object-store abort fails for one session
                log.warn("Failed aborting multipart upload for session {}: {}", s.getId(), ex.getMessage());
            }

            s.setStatus("EXPIRED");
            s.setUpdatedAt(now);
            uploadSessionRepository.save(s);
        }

        log.info("Upload cleanup: marked {} sessions as EXPIRED", candidates.size());
    }
}