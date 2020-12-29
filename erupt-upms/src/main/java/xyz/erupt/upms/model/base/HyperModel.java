package xyz.erupt.upms.model.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.SkipSerialize;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperDataProxy.class)
public class HyperModel extends BaseModel {

    @SkipSerialize
    private Date createTime;

    @SkipSerialize
    private Date updateTime;

    @SkipSerialize
    @ManyToOne(fetch = FetchType.LAZY)
    private EruptUser createUser;

    @SkipSerialize
    @ManyToOne(fetch = FetchType.LAZY)
    private EruptUser updateUser;
}
