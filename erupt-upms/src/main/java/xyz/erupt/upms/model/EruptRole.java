package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.upms.handler.RoleMenuFilter;
import xyz.erupt.upms.helper.HyperModelCreatorVo;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Erupt(name = "角色管理", dataProxy = EruptRole.class)
@EruptI18n
@Getter
@Setter
@Component
public class EruptRole extends HyperModelCreatorVo implements DataProxy<EruptRole> {

    @EruptField(
            views = @View(title = "编码", width = "100px"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "状态"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(trueText = "启用", falseText = "禁用")
            )
    )
    private Boolean status = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "e_upms_role_menu",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id"))
    @EruptField(
            views = @View(title = "菜单权限"),
            edit = @Edit(
                    filter = @Filter(conditionHandler = RoleMenuFilter.class),
                    title = "菜单权限",
                    type = EditType.TAB_TREE
            )
    )
    private Set<EruptMenu> menus;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        if (eruptUserService.getCurrentEruptUser().getIsAdmin()) return null;
        return "EruptRole.createUser.id = " + eruptUserService.getCurrentUid();
    }

}
