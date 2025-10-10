package xyz.erupt.upms.helper;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
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
public class HyperModelCreatorOnlyVo extends BaseModel {

    @ManyToOne
    @EruptField(
            views = @View(title = "创建人", width = "100px", column = "name"),
            edit = @Edit(title = "创建人", readonly = @Readonly, type = EditType.REFERENCE_TABLE)
    )
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @EruptSmartSkipSerialize
    private EruptUserVo createUser;

    @EruptField(
            views = @View(title = "创建时间", sortable = true),
            edit = @Edit(title = "创建时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    @EruptSmartSkipSerialize
    private Date createTime;

    @PrePersist
    protected void persist() {
        try {
            Optional.ofNullable(EruptSpringUtil.getBean(EruptUserService.class).getCurrentUid()).ifPresent(it -> {
                this.setCreateUser(new EruptUserVo(it));
                this.setCreateTime(new Date());
            });
        } catch (Exception ignored) {
        }
    }

}
