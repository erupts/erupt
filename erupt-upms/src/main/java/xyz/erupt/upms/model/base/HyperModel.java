package xyz.erupt.upms.model.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

    @EruptSmartSkipSerialize
    private Date createTime;

    @EruptSmartSkipSerialize
    private Date updateTime;

    @ManyToOne
    @EruptSmartSkipSerialize
    private EruptUserVo createUser;

    @ManyToOne
    @EruptSmartSkipSerialize
    private EruptUserVo updateUser;
}
