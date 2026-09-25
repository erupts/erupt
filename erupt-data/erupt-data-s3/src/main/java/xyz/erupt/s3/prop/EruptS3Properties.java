package xyz.erupt.s3.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bucket used by {@link xyz.erupt.s3.upload.S3AttachmentProxy} to store erupt
 * attachments, bound from {@code erupt.s3.*}. Credentials stay in the
 * application configuration, never in source or annotations. Registered via
 * {@code @EnableConfigurationProperties} in the auto-configuration.
 *
 * @author YuePeng
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "erupt.s3")
public class EruptS3Properties {

    /**
     * Bucket that receives uploaded attachments. Required once the proxy is in use.
     */
    private String bucket;

    /**
     * Key prefix inside the bucket, e.g. {@code erupt/}. Empty stores at the bucket root.
     */
    private String prefix = "";

    /**
     * Region name; any non-empty value works for non-AWS gateways paired with {@link #endpoint}.
     */
    private String region = "us-east-1";

    /**
     * Endpoint URL for MinIO / OSS / COS / R2. Empty uses the AWS default endpoint.
     */
    private String endpoint = "";

    /**
     * Access key. Empty falls back to the default provider chain.
     */
    private String accessKey = "";

    /**
     * Secret key, only read when {@link #accessKey} is set.
     */
    private String secretKey = "";

    /**
     * Path-style addressing ({@code endpoint/bucket/key}), required by MinIO and most self-hosted gateways.
     */
    private boolean pathStyle = false;

    /**
     * Public base URL the frontend loads attachments from, e.g. a CDN. Empty derives it
     * from {@link #endpoint} / {@link #region} and {@link #bucket}. No trailing slash needed.
     */
    private String domain = "";

    /**
     * Also keep a copy under {@code erupt.upload-path} on the local server.
     */
    private boolean localSave = false;

}
