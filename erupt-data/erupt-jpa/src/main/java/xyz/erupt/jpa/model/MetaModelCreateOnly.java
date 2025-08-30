package xyz.erupt.jpa.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.core.context.MetaContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
public class MetaModelCreateOnly extends BaseModel {

    @EruptField
    @EruptSmartSkipSerialize
    private String createBy;

    @EruptField
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @PrePersist
    protected void persist() {
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setCreateBy(it.getName());
                this.setCreateTime(LocalDateTime.now());
            }
        });
    }

}
