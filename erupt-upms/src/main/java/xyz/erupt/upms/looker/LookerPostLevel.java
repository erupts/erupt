package xyz.erupt.upms.looker;

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
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
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
 * @date 2021/3/10 11:30
 */
@MappedSuperclass
@PreDataProxy(LookerPostLevel.class)
@Service
@Getter
@Setter
public class LookerPostLevel extends BaseModel implements DataProxy<LookerPostLevel> {

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readOnly = true)
    )
    private Date createTime;

    @ManyToOne
    @EruptField(
            views = {
                    @View(title = "创建人", column = "name"),
                    @View(title = "所属组织", column = "eruptOrg.name"),
                    @View(title = "岗位", column = "eruptPost.name"),
            },
            edit = @Edit(title = "创建人", readOnly = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser createUser;

    @SkipSerialize
    private Date updateTime;

    @ManyToOne
    @SkipSerialize
    private EruptUser updateUser;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Override
    public String beforeFetch() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin()) {
            return null;
        }
        if (null == eruptUser.getEruptOrg() || null == eruptUser.getEruptPost()) {
            throw new EruptWebApiRuntimeException(eruptUser.getName() + " unbounded department cannot filter data");
        }
        String erupt = eruptUserService.getCurrentEruptMenu().getValue();
        return "(" + erupt + ".createUser.id = " + eruptUserService.getCurrentUid() + " or " + erupt + ".createUser.eruptOrg.id = " + eruptUser.getEruptOrg().getId() + " and "
                + erupt + ".createUser.eruptPost.weight < " + eruptUser.getEruptPost().getWeight() + ")";
    }

    @Override
    public void beforeAdd(LookerPostLevel lookerPostLevel) {
        lookerPostLevel.setCreateTime(new Date());
        lookerPostLevel.setCreateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }

    @Override
    public void beforeUpdate(LookerPostLevel lookerPostLevel) {
        lookerPostLevel.setUpdateTime(new Date());
        lookerPostLevel.setUpdateUser(new EruptUser(eruptUserService.getCurrentUid()));
    }
}