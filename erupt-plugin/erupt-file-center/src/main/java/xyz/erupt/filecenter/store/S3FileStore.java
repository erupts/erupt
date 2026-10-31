package xyz.erupt.filecenter.store;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import xyz.erupt.s3.S3ClientFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A bucket, or a prefix inside one, with "/" in keys read as folders.
 *
 * @author YuePeng
 */
public class S3FileStore implements FileStore {

    private static final int PAGE = 1000;

    private final String name;

    private final S3Settings prop;

    private volatile S3Client client;

    public S3FileStore(String name, S3Settings prop) {
        this.name = name;
        this.prop = prop;
    }

    @Override
    public String name() {
        return name;
    }

    private S3Client client() {
        if (null == client) {
            synchronized (this) {
                if (null == client) {
                    client = S3ClientFactory.build(prop.region(), prop.endpoint(), prop.bucket(), prop.accessKey(), prop.secretKey(), prop.pathStyle());
                }
            }
        }
        return client;
    }

    // "/a/b" -> "prefix/a/b"; the root is the prefix itself
    private String key(String path) {
        String relative = FileStore.normalize(path).substring(1);
        if (prop.prefix().isEmpty()) return relative;
        return relative.isEmpty() ? prop.prefix() : prop.prefix() + "/" + relative;
    }

    private String path(String key) {
        String relative = prop.prefix().isEmpty() ? key : Strings.CS.removeStart(key, prop.prefix() + "/");
        return "/" + Strings.CS.removeEnd(relative, "/");
    }

    @Override
    public List<FileEntry> list(String dir) {
        String prefix = this.key(dir);
        if (!prefix.isEmpty()) prefix += "/";
        List<FileEntry> entries = new ArrayList<>();
        String token = null;
        do {
            ListObjectsV2Response page = client().listObjectsV2(ListObjectsV2Request.builder().bucket(prop.bucket())
                    .prefix(prefix).delimiter("/").maxKeys(PAGE).continuationToken(token).build());
            for (CommonPrefix folder : page.commonPrefixes()) {
                String path = path(folder.prefix());
                entries.add(new FileEntry(FileStore.nameOf(path), path, true, 0, null, null));
            }
            for (S3Object object : page.contents()) {
                // the folder's own marker object is not a file
                if (object.key().equals(prefix) || object.key().endsWith("/")) continue;
                String path = path(object.key());
                entries.add(new FileEntry(FileStore.nameOf(path), path, false, object.size(),
                        null == object.lastModified() ? null : object.lastModified().toEpochMilli(), LocalFileStore.contentType(path)));
            }
            token = page.isTruncated() ? page.nextContinuationToken() : null;
        } while (null != token);
        entries.sort(Comparator.comparing((FileEntry it) -> !it.dir()).thenComparing(it -> it.name().toLowerCase()));
        return entries;
    }

    @Override
    public FileEntry stat(String path) {
        String key = this.key(path);
        if (key.isEmpty() || key.equals(prop.prefix())) return new FileEntry("", "/", true, 0, null, null);
        try {
            HeadObjectResponse head = client().headObject(HeadObjectRequest.builder().bucket(prop.bucket()).key(key).build());
            return new FileEntry(FileStore.nameOf(path), FileStore.normalize(path), false, head.contentLength(),
                    null == head.lastModified() ? null : head.lastModified().toEpochMilli(), head.contentType());
        } catch (NoSuchKeyException e) {
            // a folder is a prefix with something under it
            ListObjectsV2Response page = client().listObjectsV2(ListObjectsV2Request.builder().bucket(prop.bucket()).prefix(key + "/").maxKeys(1).build());
            return page.keyCount() > 0 ? new FileEntry(FileStore.nameOf(path), FileStore.normalize(path), true, 0, null, null) : null;
        }
    }

    @Override
    public InputStream read(String path) {
        return client().getObject(GetObjectRequest.builder().bucket(prop.bucket()).key(this.key(path)).build());
    }

    @Override
    public void write(String path, InputStream content, long size, String contentType) {
        PutObjectRequest.Builder request = PutObjectRequest.builder().bucket(prop.bucket()).key(this.key(path));
        if (null != contentType) request.contentType(contentType);
        client().putObject(request.build(), RequestBody.fromInputStream(content, size));
    }

    @Override
    public void mkdir(String path) {
        client().putObject(PutObjectRequest.builder().bucket(prop.bucket()).key(this.key(path) + "/").build(), RequestBody.empty());
    }

    private List<String> keysUnder(String key) {
        List<String> keys = new ArrayList<>();
        String token = null;
        do {
            ListObjectsV2Response page = client().listObjectsV2(ListObjectsV2Request.builder().bucket(prop.bucket()).prefix(key + "/").maxKeys(PAGE).continuationToken(token).build());
            for (S3Object object : page.contents()) keys.add(object.key());
            token = page.isTruncated() ? page.nextContinuationToken() : null;
        } while (null != token);
        return keys;
    }

    @Override
    public void rename(String path, String newName) {
        String from = this.key(path);
        String to = this.key(FileStore.join(FileStore.parentOf(path), newName));
        FileEntry entry = this.stat(path);
        if (null == entry) return;
        List<String> sources = entry.dir() ? this.keysUnder(from) : List.of(from);
        for (String source : sources) {
            String target = to + source.substring(from.length());
            client().copyObject(CopyObjectRequest.builder().sourceBucket(prop.bucket()).sourceKey(source).destinationBucket(prop.bucket()).destinationKey(target).build());
        }
        this.deleteKeys(sources);
    }

    @Override
    public void delete(String path) {
        String key = this.key(path);
        if (key.isEmpty() || key.equals(prop.prefix())) throw new IllegalArgumentException("Not the root");
        List<String> keys = new ArrayList<>(this.keysUnder(key));
        keys.add(key);
        keys.add(key + "/");
        this.deleteKeys(keys);
    }

    private void deleteKeys(List<String> keys) {
        for (int i = 0; i < keys.size(); i += PAGE) {
            List<ObjectIdentifier> batch = keys.subList(i, Math.min(i + PAGE, keys.size())).stream().map(it -> ObjectIdentifier.builder().key(it).build()).toList();
            client().deleteObjects(DeleteObjectsRequest.builder().bucket(prop.bucket()).delete(Delete.builder().objects(batch).quiet(true).build()).build());
        }
    }

    @Override
    public String publicUrl(String path) {
        String base = StringUtils.isNotBlank(prop.domain()) ? prop.domain() : this.bucketUrl();
        return Strings.CS.removeEnd(base, "/") + "/" + this.key(path);
    }

    // Mirrors S3AttachmentProxy: path-style keeps the bucket in the path, virtual-hosted puts it in front
    private String bucketUrl() {
        if (prop.endpoint().isEmpty()) return "https://" + prop.bucket() + ".s3." + prop.region() + ".amazonaws.com";
        URI endpoint = URI.create(Strings.CS.removeEnd(S3ClientFactory.endpoint(prop.endpoint(), prop.bucket(), prop.pathStyle()), "/"));
        if (prop.pathStyle()) return endpoint + "/" + prop.bucket();
        String port = endpoint.getPort() == -1 ? "" : ":" + endpoint.getPort();
        return endpoint.getScheme() + "://" + prop.bucket() + "." + endpoint.getHost() + port;
    }

}
