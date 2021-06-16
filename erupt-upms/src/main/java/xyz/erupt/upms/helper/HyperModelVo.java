package xyz.erupt.upms.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
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
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperModelVo.HyperModelDataProxy.class)
public class HyperModelVo extends BaseModel {

    @Transient
    @EruptField(
            edit = @Edit(title = "数据审计", type = EditType.DIVIDE)
    )
    private String divide;

    @ManyToOne
    @EruptField(
            views = @View(title = "创建人", column = "name"),
            edit = @Edit(title = "创建人", readonly = @Readonly, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser createUser;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readonly = @Readonly)
    )
    private Date createTime;

    @ManyToOne
    @EruptField(
            views = @View(title = "更新人", column = "name"),
            edit = @Edit(title = "更新人", readonly = @Readonly, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser updateUser;

    @EruptField(
            views = @View(title = "更新时间"),
            edit = @Edit(title = "更新时间", readonly = @Readonly)
    )
    private Date updateTime;

    @Service
    static class HyperModelDataProxy implements DataProxy<HyperModelVo> {
        @Transient
        @Resource
        private EruptUserService eruptUserService;

        @Override
        public void beforeAdd(HyperModelVo hyperModel) {
            hyperModel.setCreateTime(new Date());
            hyperModel.setCreateUser(new EruptUser(eruptUserService.getCurrentUid()));
        }

        @Override
        public void beforeUpdate(HyperModelVo hyperModel) {
            hyperModel.setUpdateTime(new Date());
            hyperModel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
        }
    }
}
