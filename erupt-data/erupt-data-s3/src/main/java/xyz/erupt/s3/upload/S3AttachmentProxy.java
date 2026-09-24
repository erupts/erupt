package xyz.erupt.s3.upload;

import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.s3.S3ClientFactory;
import xyz.erupt.s3.prop.EruptS3Properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Objects;

/**
 * Built-in {@link AttachmentProxy} that stores erupt attachments in an
 * S3-compatible bucket (AWS S3, MinIO, OSS, COS, R2, ...). Enable it with
 * {@code @EruptAttachmentUpload(S3AttachmentProxy.class)} on the Spring Boot
 * entry class and configure {@code erupt.s3.*}.
 * <p>
 * The upload path erupt generates ({@code /yyyy-MM-dd/name.ext}) is kept as the
 * object key under {@link EruptS3Properties#getPrefix()}, so
 * {@code fileDomain() + path} is the public URL of the object.
 *
 * @author YuePeng
 */
@Component
public class S3AttachmentProxy implements AttachmentProxy {

    private final EruptS3Properties prop;

    private volatile S3Client client;

    public S3AttachmentProxy(EruptS3Properties prop) {
        this.prop = prop;
    }

    @Override
    public String upLoad(InputStream inputStream, String path) {
        if (StringUtils.isBlank(prop.getBucket())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.bucket_missing"));
        }
        try {
            PutObjectRequest.Builder request = PutObjectRequest.builder().bucket(prop.getBucket()).key(this.key(path));
            String contentType = URLConnection.guessContentTypeFromName(path);
            if (null != contentType) request.contentType(contentType);
            // MultipartFile streams do not expose their length here; S3 needs it up front
            this.client().putObject(request.build(), RequestBody.fromBytes(inputStream.readAllBytes()));
        } catch (IOException | AwsServiceException | SdkClientException e) {
            String detail = e instanceof AwsServiceException aws && null != aws.awsErrorDetails()
                    ? aws.awsErrorDetails().errorMessage()
                    : Objects.toString(e.getMessage(), e.getClass().getSimpleName());
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.operation_failed") + " → " + detail);
        }
        return path;
    }

    @Override
    public String fileDomain() {
        String base = StringUtils.isNotBlank(prop.getDomain()) ? prop.getDomain() : this.bucketUrl();
        base = StringUtils.removeEnd(base, "/");
        String prefix = StringUtils.strip(prop.getPrefix(), "/");
        return prefix.isEmpty() ? base : base + "/" + prefix;
    }

    @Override
    public boolean isLocalSave() {
        return prop.isLocalSave();
    }

    /**
     * Object key for an upload path: the configured prefix followed by the path without its leading slash.
     */
    String key(String path) {
        String prefix = StringUtils.strip(prop.getPrefix(), "/");
        String name = StringUtils.removeStart(path, "/");
        return prefix.isEmpty() ? name : prefix + "/" + name;
    }

    /**
     * Public bucket URL when no explicit domain is configured: path-style keeps the bucket
     * in the path, virtual-hosted style puts it in front of the host.
     */
    private String bucketUrl() {
        if (prop.getEndpoint().isEmpty()) {
            return "https://" + prop.getBucket() + ".s3." + prop.getRegion() + ".amazonaws.com";
        }
        String configured = S3ClientFactory.endpoint(prop.getEndpoint(), prop.getBucket(), prop.isPathStyle());
        URI endpoint = URI.create(StringUtils.removeEnd(configured, "/"));
        if (prop.isPathStyle()) return endpoint + "/" + prop.getBucket();
        String port = endpoint.getPort() == -1 ? "" : ":" + endpoint.getPort();
        return endpoint.getScheme() + "://" + prop.getBucket() + "." + endpoint.getHost() + port;
    }

    private S3Client client() {
        if (null == client) {
            synchronized (this) {
                if (null == client) {
                    client = S3ClientFactory.build(prop.getRegion(), prop.getEndpoint(), prop.getBucket(), prop.getAccessKey(), prop.getSecretKey(), prop.isPathStyle());
                }
            }
        }
        return client;
    }

    @PreDestroy
    void close() {
        if (null != client) client.close();
    }

}
