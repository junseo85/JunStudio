package com.music.JunStudio.upload.storage;

import java.time.Duration;
import java.util.List;


public interface StorageMultipartService {
    MultipartInitResult initMultipart(String objectKey, String contentType);

    String presignUploadPartUrl(
            String objectKey,
            String uploadId,
            int partNumber,
            Duration ttl
    );

    void completeMultipart(String objectKey, String uploadId, List<StoragePart> parts);

    void abortMultipart(String objectKey, String uploadId);

    record MultipartInitResult(String uploadId) {}
    record StoragePart(int partNumber, String etag) {}
}