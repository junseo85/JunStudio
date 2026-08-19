package com.music.JunStudio.upload.service;
import com.music.JunStudio.upload.dto.*;
import java.util.List;
import java.util.UUID;

public interface UploadService {
    CreateUploadSessionResponse createSession(CreateUploadSessionRequest req);
    UploadSessionStatusResponse getSessionStatus(UUID sessionId);
    PresignPartsResponse presignParts(UUID sessionId, List<Integer> partNumbers);
    void completeParts(UUID sessionId, List<CompletedPartRequest> parts);
    FinalizeUploadResponse finalizeUpload(UUID sessionId);
    void abortUpload(UUID sessionId);
}