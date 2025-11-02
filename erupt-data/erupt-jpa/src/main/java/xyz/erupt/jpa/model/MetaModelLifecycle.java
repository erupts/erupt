package xyz.erupt.jpa.model;

import xyz.erupt.core.context.MetaContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Base utility class for meta model lifecycle callbacks
 * Contains common logic for persist and update operations
 * 
 * @author YuePeng
 * date 2025-11-02
 */
public abstract class MetaModelLifecycle extends BaseModel {

    /**
     * Set create by field
     * @param createBy the creator name
     */
    protected abstract void setCreateBy(String createBy);

    /**
     * Set create time field
     * @param createTime the creation time
     */
    protected abstract void setCreateTime(LocalDateTime createTime);

    /**
     * Set update by field
     * @param updateBy the updater name
     */
    protected abstract void setUpdateBy(String updateBy);

    /**
     * Set update time field
     * @param updateTime the update time
     */
    protected abstract void setUpdateTime(LocalDateTime updateTime);

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

    /**
     * Lifecycle method to initialize update metadata
     */
    protected void initializeUpdateMetadata() {
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setUpdateBy(it.getName());
                this.setUpdateTime(LocalDateTime.now());
            }
        });
    }
}
