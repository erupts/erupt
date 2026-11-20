package xyz.erupt.core.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

/**
 * The largest file the servlet container lets through, from Spring Boot's
 * {@code spring.servlet.multipart.max-file-size} and {@code max-request-size}: a bigger upload is
 * refused before any erupt code runs, so no limit erupt applies itself (site settings,
 * {@code @AttachmentType.size}) can go beyond it. Unknown or unlimited is -1.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Service
public class EruptUploadCeiling {

    @Resource
    private ObjectProvider<MultipartProperties> multipartProperties;

    /** Bytes, or -1 when Spring sets no limit (or multipart is not configured through Spring Boot) */
    public long bytes() {
        MultipartProperties properties = multipartProperties.getIfAvailable();
        if (null == properties) return -1;
        long file = bytes(properties.getMaxFileSize()), request = bytes(properties.getMaxRequestSize());
        if (file < 0) return request;
        if (request < 0) return file;
        return Math.min(file, request);
    }

    private static long bytes(DataSize size) {
        return null == size ? -1 : size.toBytes();
    }

    /** The ceiling for a message, e.g. 50MB */
    public String describe() {
        long bytes = this.bytes();
        if (bytes < 0) return "-";
        long mb = 1024 * 1024;
        return bytes % mb == 0 ? bytes / mb + "MB" : (bytes / 1024) + "KB";
    }

}
