package xyz.erupt.upms.model.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

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

    @Comment("更新时间")
    @EruptSmartSkipSerialize
    private Date updateTime;

    @Comment("创建人")
    @ManyToOne
    @EruptSmartSkipSerialize
    private EruptUserVo createUser;

    @Comment("更新人")
    @ManyToOne
    @EruptSmartSkipSerialize
    private EruptUserVo updateUser;

    @PrePersist
    protected void persist() {
        this.setCreateTime(new Date());
        try {
            this.setCreateUser(new EruptUserVo(EruptSpringUtil.getBean(EruptUserService.class).getCurrentUid()));
        } catch (Exception ignored) {
        }
        this.update();
    }

    @PreUpdate
    protected void update() {
        setUpdateTime(new Date());
        try {
            this.setUpdateUser(new EruptUserVo(EruptSpringUtil.getBean(EruptUserService.class).getCurrentUid()));
        } catch (Exception ignored) {
        }
    }
}
