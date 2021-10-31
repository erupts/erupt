package xyz.erupt.upms.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.SkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author YuePeng
 * date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperModelUpdateVo.HyperModelDataProxy.class)
public class HyperModelUpdateVo extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @SkipSerialize
    @JsonIgnore
    private EruptUserVo createUser;

    @SkipSerialize
    private Date createTime;

    @EruptField(
            views = @View(title = "更新时间", sortable = true),
            edit = @Edit(title = "更新时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @EruptField(
            views = @View(title = "更新人", width = "100px", column = "name"),
            edit = @Edit(title = "更新人", readonly = @Readonly, type = EditType.REFERENCE_TABLE)
    )
    private EruptUserVo updateUser;

    @Service
    static class HyperModelDataProxy implements DataProxy<HyperModelUpdateVo> {
        @Transient
        @Resource
        private EruptUserService eruptUserService;

        @Override
        public void beforeAdd(HyperModelUpdateVo hyperModel) {
            hyperModel.setCreateTime(new Date());
            hyperModel.setCreateUser(new EruptUserVo(eruptUserService.getCurrentUid()));
            hyperModel.setUpdateTime(new Date());
            hyperModel.setUpdateUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        }

        @Override
        public void beforeUpdate(HyperModelUpdateVo hyperModel) {
            hyperModel.setUpdateTime(new Date());
            hyperModel.setUpdateUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        }
    }
}