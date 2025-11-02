package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
public class MetaModelCreateOnly extends MetaModelLifecycle {

    @EruptField
    @EruptSmartSkipSerialize
    private String createBy;

    @EruptField
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @PrePersist
    protected void persist() {
        initializeCreateMetadata();
    }

    @Override
    protected void setUpdateBy(String updateBy) {
        // No-op: this model doesn't track updates
    }

    @Override
    protected void setUpdateTime(LocalDateTime updateTime) {
        // No-op: this model doesn't track updates
    }

}
