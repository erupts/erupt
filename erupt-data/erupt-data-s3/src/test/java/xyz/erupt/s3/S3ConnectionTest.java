package xyz.erupt.s3;

import org.junit.jupiter.api.Test;
import xyz.erupt.s3.annotation.EruptS3;
import xyz.erupt.s3.prop.EruptS3Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Empty annotation attributes fall back to erupt.s3.*; set ones win, and path-style
 * follows the endpoint it was declared with.
 */
public class S3ConnectionTest {

    @EruptS3
    static class Bare {
    }

    @EruptS3(bucket = "archive", endpoint = "https://oss-cn-hangzhou.aliyuncs.com", region = "cn-hangzhou")
    static class OwnGateway {
    }

    private static EruptS3Properties props() {
        EruptS3Properties prop = new EruptS3Properties();
        prop.setBucket("erupt-uploads");
        prop.setRegion("cn-south-1");
        prop.setEndpoint("http://minio.internal:9000");
        prop.setPathStyle(true);
        prop.setAccessKey("config-access");
        prop.setSecretKey("config-secret");
        return prop;
    }

    @Test
    public void bareAnnotationReadsEverythingFromProperties() {
        S3Connection conn = S3Connection.of(Bare.class.getAnnotation(EruptS3.class), props());
        assertEquals("erupt-uploads", conn.bucket());
        assertEquals("cn-south-1", conn.region());
        assertEquals("http://minio.internal:9000", conn.endpoint());
        assertEquals("config-access", conn.accessKey());
        assertEquals("config-secret", conn.secretKey());
        assertTrue(conn.pathStyle());
    }

    @Test
    public void annotationValuesWinAndOwnEndpointDropsConfiguredPathStyle() {
        S3Connection conn = S3Connection.of(OwnGateway.class.getAnnotation(EruptS3.class), props());
        assertEquals("archive", conn.bucket());
        assertEquals("cn-hangzhou", conn.region());
        assertEquals("https://oss-cn-hangzhou.aliyuncs.com", conn.endpoint());
        // credentials still come from the configuration
        assertEquals("config-access", conn.accessKey());
        assertFalse(conn.pathStyle());
    }

    @Test
    public void nothingConfiguredYieldsEmptyBucketAndAwsDefaults() {
        S3Connection conn = S3Connection.of(Bare.class.getAnnotation(EruptS3.class), new EruptS3Properties());
        assertEquals("", conn.bucket());
        assertEquals("us-east-1", conn.region());
        assertEquals("", conn.endpoint());
        assertEquals("", conn.accessKey());
    }

}
