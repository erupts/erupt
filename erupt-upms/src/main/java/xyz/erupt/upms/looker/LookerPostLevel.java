package xyz.erupt.upms.looker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Resource;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProxyContext;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserPostVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/10 11:30
 */
@MappedSuperclass
@PreDataProxy(LookerPostLevel.Comp.class)
@Getter
@Setter
public class LookerPostLevel extends BaseModel {

    @ManyToOne
    @EruptField(
            views = {
                    @View(title = "Creator", column = "name"),
                    @View(title = "Org", column = "eruptOrg.name"),
                    @View(title = "Post", column = "eruptPost.name"),
            },
            edit = @Edit(title = "Creator", readonly = @Readonly(allowChange = false), type = EditType.REFERENCE_TABLE)
    )
    @EruptSmartSkipSerialize
    private EruptUserPostVo createUser;

    @EruptField(
            views = @View(title = "Create Time", sortable = true),
            edit = @Edit(title = "Create Time", readonly = @Readonly(allowChange = false), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    @EruptSmartSkipSerialize
    private Date createTime;

    @EruptSmartSkipSerialize
    private Date updateTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @EruptSmartSkipSerialize
    private EruptUser updateUser;

    @Component
    static class Comp implements DataProxy<LookerPostLevel> {

        @Resource
        private EruptUserService eruptUserService;

        @Override
        public String beforeFetch(List<Condition> conditions) {
            EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
            if (eruptUser.getIsAdmin()) return null;
            if (null == eruptUser.getEruptOrg() || null == eruptUser.getEruptPost()) {
                throw new EruptWebApiRuntimeException(eruptUser.getName() + " " + I18nTranslate.$translate("upms.no_bind_post"));
            }
            String eruptName = DataProxyContext.currentClass().getSimpleName();
            return "(" + eruptName + ".createUser.id = " + eruptUserService.getCurrentUid()
                    + " or " + eruptName + ".createUser.eruptOrg.id = " + eruptUser.getEruptOrg().getId() + " and "
                    + eruptName + ".createUser.eruptPost.weight < " + eruptUser.getEruptPost().getWeight() + ")";
        }

        @Override
        public void beforeAdd(LookerPostLevel lookerPostLevel) {
            lookerPostLevel.setCreateTime(new Date());
            lookerPostLevel.setCreateUser(new EruptUserPostVo(eruptUserService.getCurrentUid()));
        }

        @Override
        public void beforeUpdate(LookerPostLevel lookerPostLevel) {
            lookerPostLevel.setUpdateTime(new Date());
            lookerPostLevel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
        }
    }


}