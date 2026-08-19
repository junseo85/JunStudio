const DEFAULTS = {
    chunkSize: 16 * 1024 * 1024, // 16MB
    concurrency: /Mobi|Android/i.test(navigator.userAgent) ? 2 : 4,
    maxRetries: 5,
    batchPresignCount: 20
};

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function retry(fn, maxRetries = 5) {
    let attempt = 0;
    while (true) {
        try {
            return await fn();
        } catch (err) {
            if (attempt >= maxRetries) throw err;
            const delay = Math.min(1000 * (2 ** attempt), 10000);
            await sleep(delay);
            attempt++;
        }
    }
}

export async function uploadLargeFile(file, options = {}) {
    const cfg = { ...DEFAULTS, ...options };
    const totalParts = Math.ceil(file.size / cfg.chunkSize);

    // 1) Create upload session
    const createRes = await fetch("/api/uploads/sessions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            fileName: file.name,
            fileSize: file.size,
            contentType: file.type || "application/octet-stream"
        })
    });
    if (!createRes.ok) {
        throw new Error(`Failed to create upload session (${createRes.status})`);
    }
    const session = await createRes.json();
    const sessionId = session.uploadSessionId;

    // 2) Check already uploaded parts (resume scenario)
    const uploadedPartNumbers = new Set();
    const statusRes = await fetch(`/api/uploads/sessions/${sessionId}`);
    if (statusRes.ok) {
        const st = await statusRes.json();
        (st.uploadedPartNumbers || []).forEach(p => uploadedPartNumbers.add(p));
    }

    const allPartNumbers = Array.from({ length: totalParts }, (_, i) => i + 1);
    const pendingParts = allPartNumbers.filter(p => !uploadedPartNumbers.has(p));
    let uploadedBytes = uploadedPartNumbers.size * cfg.chunkSize;

    const completedPartsPayload = [];

    for (let i = 0; i < pendingParts.length; i += cfg.batchPresignCount) {
        const batch = pendingParts.slice(i, i + cfg.batchPresignCount);

        // 3) Presign URLs for this batch
        const presignRes = await fetch(`/api/uploads/sessions/${sessionId}/parts/presign`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ partNumbers: batch })
        });
        if (!presignRes.ok) {
            throw new Error(`Failed to presign parts (${presignRes.status})`);
        }

        const { parts } = await presignRes.json();
        const urlMap = new Map(parts.map(p => [p.partNumber, p.url]));

        // 4) Upload parts with bounded concurrency
        const queue = [...batch];
        const workers = Array.from({ length: cfg.concurrency }, async () => {
            while (queue.length > 0) {
                const partNumber = queue.shift();
                const start = (partNumber - 1) * cfg.chunkSize;
                const end = Math.min(start + cfg.chunkSize, file.size);
                const blob = file.slice(start, end);
                const url = urlMap.get(partNumber);

                if (!url) throw new Error(`Missing presigned URL for part ${partNumber}`);

                const putRes = await retry(() => fetch(url, {
                    method: "PUT",
                    headers: { "Content-Type": "application/octet-stream" },
                    body: blob
                }), cfg.maxRetries);

                if (!putRes.ok) {
                    throw new Error(`Upload failed for part ${partNumber} (${putRes.status})`);
                }

                const etag = (putRes.headers.get("ETag") || "").replaceAll('"', "");
                completedPartsPayload.push({
                    partNumber,
                    etag,
                    checksum: null,
                    sizeBytes: end - start
                });

                uploadedBytes += (end - start);
                if (typeof cfg.onProgress === "function") {
                    cfg.onProgress({
                        uploadedBytes,
                        totalBytes: file.size,
                        percent: Math.min(100, (uploadedBytes / file.size) * 100)
                    });
                }
            }
        });

        await Promise.all(workers);

        // 5) Register completed parts in backend DB
        const completeRes = await fetch(`/api/uploads/sessions/${sessionId}/parts/complete`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ parts: completedPartsPayload })
        });
        if (!completeRes.ok) {
            throw new Error(`Failed to register uploaded parts (${completeRes.status})`);
        }
    }

    // 6) Finalize multipart upload
    const finalizeRes = await fetch(`/api/uploads/sessions/${sessionId}/finalize`, {
        method: "POST"
    });
    if (!finalizeRes.ok) {
        throw new Error(`Failed to finalize upload (${finalizeRes.status})`);
    }

    return await finalizeRes.json();
}