package xyz.erupt.s3.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import java.util.Date;
import java.util.Map;

/**
 * The fixed schema of an S3 object listing. A model bound with {@code @EruptS3} extends
 * this class and declares nothing else: the columns are defined once here and the data
 * service fills them through typed setters, so no field name travels as a string. The
 * object key is the {@code id} field, erupt's default primary key.
 * Override a field in the subclass to change its title or hide it.
 * <p>
 * {@code contentType} and {@code metadata} are only populated by {@code findDataById}
 * (an object {@code HEAD}); {@code metadata} is not rendered and is meant for
 * {@code DataProxy} / operation handlers.
 *
 * @author YuePeng
 */
@Getter
@Setter
public abstract class S3ObjectModel {

    /**
     * The object key. Named {@code id} so it is the primary key under erupt's default
     * {@code primaryKeyCol} and the model needs no extra configuration.
     */
    @EruptField(
            views = @View(title = "Key"),
            edit = @Edit(title = "Key", search = @Search(operator = QueryExpression.LIKE))
    )
    private String id;

    @EruptField(views = @View(title = "Size (bytes)"))
    private Long size;

    @EruptField(views = @View(title = "Last Modified"))
    private Date lastModified;

    @EruptField(views = @View(title = "ETag"))
    private String etag;

    @EruptField(views = @View(title = "Storage Class"))
    private String storageClass;

    @EruptField(views = @View(title = "Content Type"))
    private String contentType;

    private Map<String, String> metadata;

}
