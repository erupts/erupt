package xyz.erupt.upms.model.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperDataProxy.class)
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
}
