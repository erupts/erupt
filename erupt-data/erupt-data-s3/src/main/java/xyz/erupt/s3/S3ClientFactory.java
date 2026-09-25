package xyz.erupt.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Builds an {@link S3Client} from the connection settings shared by the data
 * source annotation and the attachment upload properties.
 *
 * @author YuePeng
 */
public class S3ClientFactory {

    private S3ClientFactory() {
    }

    public static S3Client build(String region, String endpoint, String bucket, String accessKey, String secretKey, boolean pathStyle) {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
        endpoint = endpoint(endpoint, bucket, pathStyle);
        if (!endpoint.isEmpty()) builder.endpointOverride(URI.create(endpoint));
        if (pathStyle) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        if (!accessKey.isEmpty()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    /**
     * Service endpoint for the SDK. Consoles usually show the bucket's own host
     * ({@code https://bucket.s3.region.example.com}); with virtual-hosted addressing the SDK
     * puts the bucket in front of the host again and the request lands on a bucket that does
     * not exist, so a leading {@code bucket.} label is dropped here.
     */
    public static String endpoint(String endpoint, String bucket, boolean pathStyle) {
        if (pathStyle || endpoint.isEmpty() || null == bucket || bucket.isEmpty()) return endpoint;
        URI uri = URI.create(endpoint);
        String host = uri.getHost();
        if (null == host || !host.startsWith(bucket + ".")) return endpoint;
        return endpoint.replaceFirst("//" + java.util.regex.Pattern.quote(bucket) + "\\.", "//");
    }

}
