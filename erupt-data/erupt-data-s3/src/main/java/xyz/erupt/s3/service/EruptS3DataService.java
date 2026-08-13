package xyz.erupt.s3.service;

import com.google.gson.Gson;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.s3.annotation.EruptS3;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S3-compatible object storage data source using AWS SDK v2. Each object under
 * the configured bucket / prefix becomes one row exposing {@code key},
 * {@code size}, {@code lastModified}, {@code etag} and {@code storageClass};
 * {@code findDataById} additionally reads {@code contentType} and user
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
public class EruptS3DataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "S3";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptS3DataService.class);
    }

    private final Map<String, S3Client> clients = new ConcurrentHashMap<>();

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptS3 eruptS3 = this.eruptS3(eruptModel);
        S3Client client = this.client(eruptS3);
        List<Map<String, Object>> rows = new ArrayList<>();
        String continuationToken = null;
        int remaining = eruptS3.maxObjects();
        try {
            do {
                ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
                        .bucket(eruptS3.bucket())
                        .maxKeys(Math.min(eruptS3.pageSize(), remaining));
                if (!eruptS3.prefix().isEmpty()) request.prefix(eruptS3.prefix());
                if (null != continuationToken) request.continuationToken(continuationToken);
                ListObjectsV2Response response = client.listObjectsV2(request.build());
                for (S3Object object : response.contents()) {
                    rows.add(this.toRow(object));
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
        EruptS3 eruptS3 = this.eruptS3(eruptModel);
        String key = String.valueOf(id);
        try {
            HeadObjectResponse head = this.client(eruptS3).headObject(b -> b.bucket(eruptS3.bucket()).key(key));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", key);
            row.put("size", head.contentLength());
            row.put("lastModified", null == head.lastModified() ? null : Date.from(head.lastModified()));
            row.put("etag", head.eTag());
            row.put("storageClass", null == head.storageClassAsString() ? null : head.storageClassAsString());
            row.put("contentType", head.contentType());
            row.put("metadata", head.metadata());
            Gson gson = GsonFactory.getGson();
            return gson.fromJson(gson.toJson(row), eruptModel.getClazz());
        } catch (NoSuchKeyException e) {
            return null;
        } catch (S3Exception | SdkClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptS3 eruptS3 = this.eruptS3(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("s3.primary_key_missing"));
        try {
            this.client(eruptS3).deleteObject(b -> b.bucket(eruptS3.bucket()).key(String.valueOf(id)));
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

    private S3Client client(EruptS3 eruptS3) {
        String key = eruptS3.endpoint() + "|" + eruptS3.region() + "|" + eruptS3.accessKey() + "|" + eruptS3.pathStyle();
        return clients.computeIfAbsent(key, k -> this.buildClient(eruptS3));
    }

    private S3Client buildClient(EruptS3 eruptS3) {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(eruptS3.region()));
        if (!eruptS3.endpoint().isEmpty()) builder.endpointOverride(URI.create(eruptS3.endpoint()));
        if (eruptS3.pathStyle()) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        if (!eruptS3.accessKey().isEmpty()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(eruptS3.accessKey(), eruptS3.secretKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    private Map<String, Object> toRow(S3Object object) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", object.key());
        row.put("size", object.size());
        row.put("lastModified", null == object.lastModified() ? null : Date.from(object.lastModified()));
        row.put("etag", object.eTag());
        row.put("storageClass", null == object.storageClassAsString() ? null : object.storageClassAsString());
        return row;
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
