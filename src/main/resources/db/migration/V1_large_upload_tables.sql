CREATE TABLE IF NOT EXISTS upload_session (
                                              id UUID PRIMARY KEY,
                                              user_id VARCHAR(128) NOT NULL,
    file_name VARCHAR(1024) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    object_key VARCHAR(2048) NOT NULL UNIQUE,
    provider_upload_id VARCHAR(512) NOT NULL,
    chunk_size_bytes BIGINT NOT NULL,
    total_parts INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_upload_session_user_status_updated
    ON upload_session(user_id, status, updated_at);

CREATE TABLE IF NOT EXISTS upload_part (
                                           upload_session_id UUID NOT NULL,
                                           part_number INT NOT NULL,
                                           etag VARCHAR(512) NOT NULL,
    checksum VARCHAR(512),
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    PRIMARY KEY (upload_session_id, part_number),
    CONSTRAINT fk_upload_part_session
    FOREIGN KEY (upload_session_id) REFERENCES upload_session(id)
    );

CREATE TABLE IF NOT EXISTS media_file (
                                          id BIGSERIAL PRIMARY KEY,
                                          user_id VARCHAR(128) NOT NULL,
    object_key VARCHAR(2048) NOT NULL UNIQUE,
    file_name VARCHAR(1024) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    upload_session_id UUID NOT NULL UNIQUE,
    processing_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );