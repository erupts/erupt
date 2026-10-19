package xyz.erupt.filecenter.store;

/**
 * What an S3 store needs: a bucket, how to reach it, and optionally a key prefix that becomes the
 * store's root and a public domain for links.
 *
 * @author YuePeng
 */
public record S3Settings(String bucket, String region, String endpoint, String accessKey, String secretKey,
                         boolean pathStyle, String prefix, String domain) {

    public S3Settings {
        region = blank(region) ? "us-east-1" : region;
        endpoint = null == endpoint ? "" : endpoint;
        accessKey = null == accessKey ? "" : accessKey;
        secretKey = null == secretKey ? "" : secretKey;
        prefix = null == prefix ? "" : prefix.replaceAll("^/+|/+$", "");
        domain = null == domain ? "" : domain;
    }

    private static boolean blank(String value) {
        return null == value || value.isBlank();
    }

}
