package xyz.erupt.upms.helper;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@EruptI18n
public class HyperModelCreatorVo extends BaseModel {

    @ManyToOne
    @EruptField(
            views = @View(title = "创建人", width = "100px", column = "name"),
            edit = @Edit(title = "创建人", readonly = @Readonly, type = EditType.REFERENCE_TABLE)
    )
    @EruptSmartSkipSerialize
    private EruptUserVo createUser;

    @EruptField(
            views = @View(title = "创建时间", sortable = true),
            edit = @Edit(title = "创建时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    @EruptSmartSkipSerialize
    private Date createTime;

    @Comment("更新时间")
    @EruptSmartSkipSerialize
    private Date updateTime;

    @Comment("更新人")
    @ManyToOne
    @EruptSmartSkipSerialize
    private EruptUserVo updateUser;

    @PrePersist
    protected void persist() {
        try {
            Optional.ofNullable(EruptSpringUtil.getBean(EruptUserService.class).getCurrentUid()).ifPresent(it -> {
                this.setCreateUser(new EruptUserVo(it));
                this.setCreateTime(new Date());
            });
        } catch (Exception ignored) {
        }
        this.update();
    }

    @PreUpdate
    protected void update() {
        try {
            Optional.ofNullable(EruptSpringUtil.getBean(EruptUserService.class).getCurrentUid()).ifPresent(it -> {
                this.setUpdateUser(new EruptUserVo(it));
                this.setUpdateTime(new Date());
            });
        } catch (Exception ignored) {
        }
    }

}