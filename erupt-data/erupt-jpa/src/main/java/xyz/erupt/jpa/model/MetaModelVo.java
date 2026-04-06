package xyz.erupt.jpa.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
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
@EruptI18n
public class MetaModelVo extends BaseModel {

    @Transient
    @EruptField(
            edit = @Edit(title = "Data Audit", type = EditType.DIVIDE)
    )
    @EruptSmartSkipSerialize
    private String divide;

    @EruptField(
            views = @View(title = "Creator", width = "100px"),
            edit = @Edit(title = "Creator", readonly = @Readonly(allowChange = false))
    )
    @EruptSmartSkipSerialize
    private String createBy;

    @EruptField(
            views = @View(title = "Create Time", sortable = true),
            edit = @Edit(title = "Create Time", readonly = @Readonly(allowChange = false), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @EruptField(
            views = @View(title = "Updater", width = "100px"),
            edit = @Edit(title = "Updater", readonly = @Readonly(allowChange = false))
    )
    @EruptSmartSkipSerialize
    private String updateBy;

    @EruptField(
            views = @View(title = "Update Time", sortable = true),
            edit = @Edit(title = "Update Time", readonly = @Readonly(allowChange = false), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    @EruptSmartSkipSerialize
    private LocalDateTime updateTime;

    @PrePersist
    protected void persist() {
        this.setCreateTime(LocalDateTime.now());
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setCreateBy(it.getName());
            }
        });
        this.update();
    }

    @PreUpdate
    protected void update() {
        this.setUpdateTime(LocalDateTime.now());
        Optional.ofNullable(MetaContext.getUser()).ifPresent(it -> {
            if (null != it.getName()) {
                this.setUpdateBy(it.getName());
            }
        });
    }


}
