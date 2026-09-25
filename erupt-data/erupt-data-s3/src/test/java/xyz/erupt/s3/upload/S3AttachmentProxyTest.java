package xyz.erupt.s3.upload;

import org.junit.jupiter.api.Test;
import xyz.erupt.s3.S3ClientFactory;
import xyz.erupt.s3.prop.EruptS3Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@code fileDomain() + path} must resolve to the object written under {@code key(path)}
 * for every addressing style, without touching a real bucket.
 */
public class S3AttachmentProxyTest {

    private static final String PATH = "/2026-09-24/abc.png";

    private static EruptS3Properties props() {
        EruptS3Properties prop = new EruptS3Properties();
        prop.setBucket("erupt-uploads");
        return prop;
    }

    @Test
    public void awsVirtualHosted() {
        EruptS3Properties prop = props();
        prop.setRegion("ap-southeast-1");
        S3AttachmentProxy proxy = new S3AttachmentProxy(prop);
        assertEquals("https://erupt-uploads.s3.ap-southeast-1.amazonaws.com", proxy.fileDomain());
        assertEquals("2026-09-24/abc.png", proxy.key(PATH));
    }

    @Test
    public void minioPathStyleWithPrefix() {
        EruptS3Properties prop = props();
        prop.setEndpoint("http://minio.internal:9000/");
        prop.setPathStyle(true);
        prop.setPrefix("/erupt/");
        S3AttachmentProxy proxy = new S3AttachmentProxy(prop);
        assertEquals("http://minio.internal:9000/erupt-uploads/erupt", proxy.fileDomain());
        assertEquals("erupt/2026-09-24/abc.png", proxy.key(PATH));
    }

    @Test
    public void customEndpointVirtualHosted() {
        EruptS3Properties prop = props();
        prop.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        S3AttachmentProxy proxy = new S3AttachmentProxy(prop);
        assertEquals("https://erupt-uploads.oss-cn-hangzhou.aliyuncs.com", proxy.fileDomain());
    }

    // A bucket URL copied from a console is accepted as the endpoint: the SDK gets the service
    // host, and the derived public URL does not double the bucket label
    @Test
    public void bucketHostEndpointIsReducedToServiceHost() {
        EruptS3Properties prop = props();
        prop.setEndpoint("http://erupt-uploads.s3.cn-south-1.qiniucs.com");
        S3AttachmentProxy proxy = new S3AttachmentProxy(prop);
        assertEquals("http://s3.cn-south-1.qiniucs.com", S3ClientFactory.endpoint(prop.getEndpoint(), prop.getBucket(), false));
        assertEquals("http://erupt-uploads.s3.cn-south-1.qiniucs.com", proxy.fileDomain());
        // path-style keeps the endpoint untouched: the bucket belongs in the path there
        assertEquals("http://minio.internal:9000", S3ClientFactory.endpoint("http://minio.internal:9000", "minio", true));
        // a host that merely starts with the bucket name is not a bucket label
        assertEquals("https://erupt-uploadsx.example.com", S3ClientFactory.endpoint("https://erupt-uploadsx.example.com", "erupt-uploads", false));
    }

    @Test
    public void explicitDomainWinsAndCarriesPrefix() {
        EruptS3Properties prop = props();
        prop.setDomain("https://cdn.erupt.xyz/");
        prop.setPrefix("erupt");
        S3AttachmentProxy proxy = new S3AttachmentProxy(prop);
        assertEquals("https://cdn.erupt.xyz/erupt", proxy.fileDomain());
        assertEquals("https://cdn.erupt.xyz/erupt" + PATH, proxy.fileDomain() + PATH);
        assertFalse(proxy.isLocalSave());
    }

}
