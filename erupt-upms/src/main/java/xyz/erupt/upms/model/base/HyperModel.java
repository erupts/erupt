package xyz.erupt.upms.model.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
public class HyperModel extends BaseModel {

    @Comment("创建时间")
    @EruptSmartSkipSerialize
    private Date createTime;

    @Comment("创建人")
    @ManyToOne
    @EruptSmartSkipSerialize
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo createUser;

    @Comment("更新时间")
    @EruptSmartSkipSerialize
    private Date updateTime;

    @Comment("更新人")
    @ManyToOne
    @EruptSmartSkipSerialize
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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
