package xyz.erupt.s3;

import xyz.erupt.s3.annotation.EruptS3;
import xyz.erupt.s3.prop.EruptS3Properties;

/**
 * Connection settings of one {@link EruptS3} model. An attribute left empty on the
 * annotation is taken from {@code erupt.s3.*}, so credentials never have to appear in
 * source: a model that only names its bucket (or nothing at all) reads the bucket the
 * attachment proxy is configured for.
 *
 * @author YuePeng
 */
public record S3Connection(String bucket, String region, String endpoint, String accessKey, String secretKey, boolean pathStyle) {

    public static S3Connection of(EruptS3 eruptS3, EruptS3Properties prop) {
        boolean ownEndpoint = !eruptS3.endpoint().isEmpty();
        boolean ownCredentials = !eruptS3.accessKey().isEmpty();
        return new S3Connection(
                firstNonEmpty(eruptS3.bucket(), prop.getBucket()),
                firstNonEmpty(eruptS3.region(), prop.getRegion()),
                ownEndpoint ? eruptS3.endpoint() : prop.getEndpoint(),
                ownCredentials ? eruptS3.accessKey() : prop.getAccessKey(),
                ownCredentials ? eruptS3.secretKey() : prop.getSecretKey(),
                // path-style belongs to the endpoint it is used with
                ownEndpoint ? eruptS3.pathStyle() : prop.isPathStyle()
        );
    }

    /**
     * Cache key for the client: everything but the bucket, which is a per-request attribute.
     */
    public String clientKey() {
        return endpoint + "|" + region + "|" + accessKey + "|" + pathStyle;
    }

    private static String firstNonEmpty(String preferred, String fallback) {
        return null != preferred && !preferred.isEmpty() ? preferred : null == fallback ? "" : fallback;
    }

}
