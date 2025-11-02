package xyz.erupt.jpa.model;

import xyz.erupt.core.context.MetaContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Base utility class for create-only meta model lifecycle callbacks
 * Contains common logic for persist operations only
 * 
 * @author YuePeng
 * date 2025-11-02
 */
public abstract class MetaModelCreateOnlyLifecycle extends BaseModel {

    /**
     * Set create by field
     * @param createBy the creator name
     */
    public abstract void setCreateBy(String createBy);

    /**
     * Set create time field
     * @param createTime the creation time
     */
    public abstract void setCreateTime(LocalDateTime createTime);

    /**
     * Lifecycle method to initialize create metadata
     */
    protected void initializeCreateMetadata() {
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setCreateBy(it.getName());
                this.setCreateTime(LocalDateTime.now());
            }
        });
    }
}
