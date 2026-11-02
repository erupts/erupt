package xyz.erupt.s3.service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.s3.S3ClientFactory;
import xyz.erupt.s3.S3Connection;
import xyz.erupt.s3.prop.EruptS3Properties;
import xyz.erupt.s3.annotation.EruptS3;
import xyz.erupt.s3.model.S3ObjectModel;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S3-compatible object storage data source using AWS SDK v2. Each object under
 * the configured bucket / prefix becomes one instance of the model, which must
 * extend {@link S3ObjectModel}: the listing fills key, size, lastModified, etag
 * and storageClass; {@code findDataById} additionally reads contentType and user
 * metadata via {@code HEAD}.
 * <p>
 * The same {@code S3Client} is cached per (endpoint, region, credential) tuple
 * so bucket-level configuration changes still reuse the underlying HTTP client.
 * Adds and edits are not supported — uploading raw object content through an
 * admin form conflates too many things with too little payoff.
 *
 * @author YuePeng
 */
@Service
public class EruptS3DataService extends EruptBeanDataService<S3ObjectModel> {

    public static final String DATA_PROCESSOR = "S3";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptS3DataService.class);
    }

    private final Map<String, S3Client> clients = new ConcurrentHashMap<>();

    @Resource
    private EruptS3Properties prop;

    @Override
    protected List<S3ObjectModel> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptS3 eruptS3 = this.eruptS3(eruptModel);
        S3Connection conn = this.connection(eruptS3);
        S3Client client = this.client(conn);
        List<S3ObjectModel> rows = new ArrayList<>();
        String continuationToken = null;
        int remaining = eruptS3.maxObjects();
        try {
            do {
                ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
                        .bucket(conn.bucket())
                        .maxKeys(Math.min(eruptS3.pageSize(), remaining));
                if (!eruptS3.prefix().isEmpty()) request.prefix(eruptS3.prefix());
                if (null != continuationToken) request.continuationToken(continuationToken);
                ListObjectsV2Response response = client.listObjectsV2(request.build());
                for (S3Object object : response.contents()) {
                    rows.add(apply(this.newModel(eruptModel), object));
                    if (--remaining <= 0) return rows;
                }
                continuationToken = Boolean.TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
            } while (null != continuationToken && remaining > 0);
        } catch (S3Exception | SdkClientException e) {
            throw this.wrap(e);
        }
        return rows;
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        S3Connection conn = this.connection(this.eruptS3(eruptModel));
        String key = String.valueOf(id);
        try {
            HeadObjectResponse head = this.client(conn).headObject(b -> b.bucket(conn.bucket()).key(key));
            S3ObjectModel model = this.newModel(eruptModel);
            model.setId(key);
            model.setSize(head.contentLength());
            model.setLastModified(toDate(head.lastModified()));
            model.setEtag(head.eTag());
            model.setStorageClass(head.storageClassAsString());
            model.setContentType(head.contentType());
            model.setMetadata(head.metadata());
            return model;
        } catch (NoSuchKeyException e) {
            return null;
        } catch (S3Exception | SdkClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        S3Connection conn = this.connection(this.eruptS3(eruptModel));
        // the object was materialized by findDataById above, so it is one of our models
        String key = object instanceof S3ObjectModel model ? model.getId() : null;
        if (null == key) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.primary_key_missing"));
        try {
            this.client(conn).deleteObject(b -> b.bucket(conn.bucket()).key(key));
        } catch (S3Exception | SdkClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.read_only_edit"));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.read_only_edit"));
    }

    private EruptS3 eruptS3(EruptModel eruptModel) {
        EruptS3 eruptS3 = eruptModel.getClazz().getAnnotation(EruptS3.class);
        if (null == eruptS3) {
            throw new EruptWebApiRuntimeException("@EruptS3 annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptS3;
    }

    private S3Connection connection(EruptS3 eruptS3) {
        S3Connection conn = S3Connection.of(eruptS3, prop);
        if (conn.bucket().isEmpty()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.bucket_missing"));
        return conn;
    }

    private S3Client client(S3Connection conn) {
        return clients.computeIfAbsent(conn.clientKey(), k ->
                S3ClientFactory.build(conn.region(), conn.endpoint(), conn.bucket(), conn.accessKey(), conn.secretKey(), conn.pathStyle()));
    }

    /**
     * A fresh instance of the bound model, which has to extend {@link S3ObjectModel} so the
     * listing can be written through its setters.
     */
    private S3ObjectModel newModel(EruptModel eruptModel) {
        Class<?> clazz = eruptModel.getClazz();
        if (!S3ObjectModel.class.isAssignableFrom(clazz)) {
            throw new EruptWebApiRuntimeException(clazz.getName() + " must extend " + S3ObjectModel.class.getName() + " to use the S3 data source");
        }
        try {
            return (S3ObjectModel) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EruptWebApiRuntimeException(clazz.getName() + " needs a public no-arg constructor: " + e.getMessage());
        }
    }

    static S3ObjectModel apply(S3ObjectModel model, S3Object object) {
        model.setId(object.key());
        model.setSize(object.size());
        model.setLastModified(toDate(object.lastModified()));
        model.setEtag(object.eTag());
        model.setStorageClass(object.storageClassAsString());
        return model;
    }

    private static Date toDate(Instant instant) {
        return null == instant ? null : Date.from(instant);
    }

    private EruptWebApiRuntimeException wrap(Exception e) {
        String detail = e instanceof AwsServiceException aws && null != aws.awsErrorDetails()
                ? aws.awsErrorDetails().errorMessage()
                : Objects.toString(e.getMessage(), e.getClass().getSimpleName());
        return new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.operation_failed") + " → " + detail);
    }

    @PreDestroy
    void closeClients() {
        clients.values().forEach(S3Client::close);
        clients.clear();
    }

}
