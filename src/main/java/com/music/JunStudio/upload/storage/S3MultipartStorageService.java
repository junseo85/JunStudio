package com.music.JunStudio.upload.storage;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;





import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3MultipartStorageService implements StorageMultipartService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.storage.s3.bucket}")
    private String bucket;

    @Override
    public MultipartInitResult initMultipart(String objectKey, String contentType) {
        CreateMultipartUploadResponse res = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(contentType)
                        .build()
        );
        return new MultipartInitResult(res.uploadId());
    }

    @Override
    public String presignUploadPartUrl(String objectKey, String uploadId, int partNumber, Duration ttl) {
        UploadPartRequest req = UploadPartRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignReq = UploadPartPresignRequest.builder()
                .signatureDuration(ttl)
                .uploadPartRequest(req)
                .build();

        return s3Presigner.presignUploadPart(presignReq).url().toString();

    }

    @Override
    public void completeMultipart(String objectKey, String uploadId, List<StoragePart> parts) {
        List<CompletedPart> completed = parts.stream()
                .map(p -> CompletedPart.builder().partNumber(p.partNumber()).eTag(p.etag()).build())
                .toList();

        s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .multipartUpload(CompletedMultipartUpload.builder().parts(completed).build())
                        .build()
        );
    }

    @Override
    public void abortMultipart(String objectKey, String uploadId) {
        s3Client.abortMultipartUpload(
                AbortMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .build()
        );
    }
}