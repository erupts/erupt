# erupt-data-s3

S3-compatible object storage data source for Erupt, built on AWS SDK v2. Bind a `@Erupt` model to a bucket (AWS S3, MinIO, Aliyun OSS, Tencent COS, Cloudflare R2, ...) and Erupt gives you a permissioned, searchable, auditable admin view of objects — with the standard delete flow wired up.

Read + delete only. Uploading raw object content through an admin form is not a good fit; use the S3 SDK / your app's upload flow directly.

## Annotation

`@EruptS3`

| Attribute    | Default        | Description                                                                                    |
|--------------|----------------|------------------------------------------------------------------------------------------------|
| `bucket`     | —              | Bucket to list                                                                                 |
| `prefix`     | `""`           | Key prefix filter                                                                              |
| `region`     | `"us-east-1"`  | Region name — required by AWS; for non-AWS providers, any non-empty value paired with `endpoint` |
| `endpoint`   | `""`           | Endpoint URL; empty = AWS default endpoint for `region`                                        |
| `accessKey`  | `""`           | Access key; empty = default provider chain (env / `~/.aws/credentials` / instance profile)     |
| `secretKey`  | `""`           | Secret key; only read when `accessKey` is set                                                  |
| `pathStyle`  | `false`        | Force path-style addressing — required by MinIO and older OSS gateways                          |
| `pageSize`   | `1000`         | Max objects returned per list call                                                             |
| `maxObjects` | `5000`         | Hard cap on total objects across all pages                                                     |

## Available model fields

| Field           | Type              | Populated in                    |
|-----------------|-------------------|---------------------------------|
| `key`           | `String`          | list + find                     |
| `size`          | `Long`            | list + find                     |
| `lastModified`  | `Date`            | list + find                     |
| `etag`          | `String`          | list + find                     |
| `storageClass`  | `String`          | list + find                     |
| `contentType`   | `String`          | find only                       |
| `metadata`      | `Map<String,String>` | find only (x-amz-meta-* headers) |

## Example — AWS S3

```java
@Getter
@Setter
@Erupt(name = "S3 Objects", primaryKeyCol = "key")
@EruptS3(bucket = "prod-uploads", prefix = "reports/", region = "us-east-1")
@EruptDataProcessor(EruptS3DataService.DATA_PROCESSOR)
public class S3ProductionUpload {

    @EruptField(views = @View(title = "Key"))
    private String key;

    @EruptField(views = @View(title = "Size (bytes)"))
    private Long size;

    @EruptField(views = @View(title = "Last Modified"))
    private Date lastModified;

    @EruptField(views = @View(title = "ETag"))
    private String etag;

    @EruptField(views = @View(title = "Storage Class"))
    private String storageClass;
}
```

## Example — MinIO / self-hosted

```java
@EruptS3(
    bucket = "erupt-uploads",
    endpoint = "http://minio.internal:9000",
    region = "us-east-1",
    accessKey = "AKIAxxx",
    secretKey = "xxx",
    pathStyle = true
)
```

## Example — other providers

| Provider              | `endpoint`                                         | `pathStyle` |
|-----------------------|----------------------------------------------------|-------------|
| Aliyun OSS            | `https://oss-cn-hangzhou.aliyuncs.com`             | `false`     |
| Tencent COS           | `https://cos.ap-guangzhou.myqcloud.com`            | `false`     |
| Cloudflare R2         | `https://<account>.r2.cloudflarestorage.com`       | `true`      |
| Backblaze B2 (S3 API) | `https://s3.<region>.backblazeb2.com`              | `true`      |

## Operations

- **List**: `ListObjectsV2` with continuation-token paging, bounded by `maxObjects`.
- **Find by id**: `HeadObject` — populates the extra `contentType` and `metadata` fields.
- **Delete**: `DeleteObject` on the key.
- **Add / edit**: not supported.

## Gotchas

- The `key` field is the S3 object key verbatim and is the primary key — do not rename the field.
- On huge buckets (>5000 objects under the prefix) results are truncated; narrow with `prefix` or raise `maxObjects` explicitly.
- `metadata` is only populated on find (HEAD), not in the list view — showing it in the list column will render empty.
