package xyz.erupt.s3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to objects inside an S3-compatible bucket. Place
 * alongside {@code @EruptDataProcessor(EruptS3DataService.DATA_PROCESSOR)}.
 * <p>
 * Each object listed under the configured {@link #prefix()} becomes one row.
 * The model's primary key column supplies the object key. Available fields:
 * {@code key}, {@code size}, {@code lastModified}, {@code etag},
 * {@code storageClass}, plus (on {@code findDataById}) {@code contentType} and
 * {@code metadata} from the object's {@code HEAD} response.
 * <p>
 * Add and in-place edit are not supported; use {@code putObject} in your
 * client for uploads. Delete works by key.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptS3 {

    /**
     * Bucket to list.
     */
    String bucket();

    /**
     * Optional key prefix — objects under this prefix are listed. Empty lists
     * the whole bucket.
     */
    String prefix() default "";

    /**
     * Region name, e.g. {@code us-east-1}, {@code ap-southeast-1}. Required for
     * AWS; for MinIO / OSS / COS any non-empty value paired with {@link #endpoint()}.
     */
    String region() default "us-east-1";

    /**
     * Endpoint URL. Empty uses the AWS default endpoint for {@link #region()}.
     * Set this for MinIO / OSS / COS / R2, e.g. {@code https://oss-cn-hangzhou.aliyuncs.com}.
     */
    String endpoint() default "";

    /**
     * Access key. Empty falls back to the default provider chain
     * (env vars, {@code ~/.aws/credentials}, instance profile).
     */
    String accessKey() default "";

    /**
     * Secret key. Only read when {@link #accessKey()} is set.
     */
    String secretKey() default "";

    /**
     * Force path-style addressing ({@code https://endpoint/bucket/key}).
     * Required by most non-AWS gateways (MinIO, older OSS gateways).
     */
    boolean pathStyle() default false;

    /**
     * Maximum objects returned by a single list call. Multiple pages are still
     * fetched up to {@link #maxObjects()} in total.
     */
    int pageSize() default 1000;

    /**
     * Hard cap on objects returned across all pages, to avoid runaway listings
     * on huge buckets.
     */
    int maxObjects() default 5000;

}
