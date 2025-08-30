package xyz.erupt.jpa.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.context.MetaContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2021/7/2 10:23
 */
@Getter
@Setter
@MappedSuperclass
@EruptI18n
public class MetaModelCreateVo extends BaseModel {

    @EruptField(
            views = @View(title = "创建人", width = "100px"),
            edit = @Edit(title = "创建人", readonly = @Readonly)
    )
    private String createBy;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime createTime;

    @EruptField(views = @View(title = "更新人", show = false))
    @EruptSmartSkipSerialize
    private String updateBy;

    @EruptField(views = @View(title = "更新时间", show = false))
    @EruptSmartSkipSerialize
    private LocalDateTime updateTime;

    @PrePersist
    protected void persist() {
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setCreateBy(it.getName());
                this.setCreateTime(LocalDateTime.now());
            }
        });
        this.update();
    }

    @PreUpdate
    protected void update() {
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setUpdateBy(it.getName());
                this.setUpdateTime(LocalDateTime.now());
            }
        });
    }

}