package xyz.erupt.upms.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.SkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperModelCreatorVo.class)
@Service
public class HyperModelCreatorVo extends BaseModel implements DataProxy<HyperModelCreatorVo> {

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readOnly = true)
    )
    private Date createTime;

    @ManyToOne
    @EruptField(
            views = @View(title = "创建人", column = "name"),
            edit = @Edit(title = "创建人", readOnly = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser createUser;

    @SkipSerialize
    private Date updateTime;

    @ManyToOne
    @SkipSerialize
    private EruptUser updateUser;

    @Transient
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(HyperModelCreatorVo hyperModel) {
        hyperModel.setCreateTime(new Date());
        hyperModel.setCreateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }

    @Override
    public void beforeUpdate(HyperModelCreatorVo hyperModel) {
        hyperModel.setUpdateTime(new Date());
        hyperModel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }

}