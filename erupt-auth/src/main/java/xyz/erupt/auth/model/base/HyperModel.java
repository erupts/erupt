package xyz.erupt.auth.model.base;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.SkipSerialize;
import xyz.erupt.auth.model.EruptUser;

import javax.persistence.*;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperDataProxy.class)
public class HyperModel {

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "ID")
    @EruptField
    private Long id;

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
