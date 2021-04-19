package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.MD5Utils;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account")
})
@Erupt(
        name = "用户", desc = "用户配置",
        dataProxy = EruptUser.class,
        linkTree = @LinkTree(field = "eruptOrg")
)
@Getter
@Setter
@Component
public class EruptUser extends HyperModel implements DataProxy<EruptUser> {

    private static final String EMAIL_REGEX = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    private static final String PHONE_REGEX = "^[1][3,4,5,6,7,8,9][0-9]{9}$";

    @EruptField(
            views = @View(title = "用户名", sortable = true),
            edit = @Edit(title = "用户名", desc = "登录用户名", notNull = true, search = @Search(vague = true))
    )
    private String account;

    @EruptField(
            views = @View(title = "姓名", sortable = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "账户状态"),
            edit = @Edit(
                    title = "账户状态",
                    search = @Search,
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "激活",
                            falseText = "锁定"
                    )
            )
    )
    private Boolean status = true;

    @EruptField(
            views = @View(title = "手机号码"),
            edit = @Edit(title = "手机号码", search = @Search(vague = true), inputType = @InputType(regex = PHONE_REGEX))
    )
    private String phone;

    @EruptField(
            views = @View(title = "邮箱"),
            edit = @Edit(title = "邮箱", search = @Search(vague = true), inputType = @InputType(regex = EMAIL_REGEX))
    )
    private String email;

    @ManyToOne
    @EruptField(
            views = @View(title = "首页地址", column = "name"),
            edit = @Edit(
                    title = "首页地址",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu eruptMenu;

    @ManyToOne
    @EruptField(
            views = @View(title = "所属组织", column = "name"),
            edit = @Edit(title = "所属组织", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id"))
    )
    private EruptOrg eruptOrg;

    @ManyToOne
    @EruptField(
            views = @View(title = "岗位", column = "name"),
            edit = @Edit(title = "岗位", type = EditType.REFERENCE_TREE)
    )
    private EruptPost eruptPost;

    @Transient
    @EruptField(
            edit = @Edit(title = "密码", type = EditType.DIVIDE)
    )
    private String pwdDivide;

    private String password;

    @Transient
    @EruptField(
            edit = @Edit(title = "密码")
    )
    private String passwordA;

    @Transient
    @EruptField(
            edit = @Edit(title = "确认密码")
    )
    private String passwordB;

    @EruptField(
            views = @View(title = "md5加密"),
            edit = @Edit(
                    title = "md5加密",
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    )
            )
    )
    private Boolean isMd5 = true;

    @ManyToMany
    @JoinTable(
            name = "e_upms_user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @EruptField(
            views = @View(title = "所属角色"),
            edit = @Edit(
                    title = "所属角色",
                    type = EditType.CHECKBOX
            )
    )
    private Set<EruptRole> roles;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "ip白名单",
                    desc = "ip与ip之间使用换行符间隔，不填表示不鉴权",
                    type = EditType.TEXTAREA
            )

    )
    private String whiteIp;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "备注",
                    type = EditType.TEXTAREA
            )
    )
    private String remark;

    private Boolean isAdmin;

    @Transient
    @PersistenceContext
    private EntityManager entityManager;

    @Transient
    @Resource
    private EruptUserService eruptUserService;

    public EruptUser() {
    }

    public EruptUser(Long id) {
        this.setId(id);
    }


    @Override
    public void beforeAdd(EruptUser eruptUser) {
        if (StringUtils.isBlank(eruptUser.getPasswordA())) {
            throw new EruptApiErrorTip(EruptApiModel.Status.WARNING, "密码必填", EruptApiModel.PromptWay.MESSAGE);
        }
        this.checkPostOrg(eruptUser);
        if (eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
            eruptUser.setIsAdmin(false);
            eruptUser.setCreateTime(new Date());
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Utils.digest(eruptUser.getPasswordA()));
            }
        } else {
            throw new EruptWebApiRuntimeException("两次密码输入不一致");
        }
    }

    @Override
    public void beforeUpdate(EruptUser eruptUser) {
        entityManager.clear();
        EruptUser eu = entityManager.find(EruptUser.class, eruptUser.getId());
        if (!eruptUser.getIsMd5() && eu.getIsMd5()) {
            throw new EruptWebApiRuntimeException("MD5 不可逆");
        }
        this.checkPostOrg(eruptUser);
        if (StringUtils.isNotBlank(eruptUser.getPasswordA())) {
            if (!eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
                throw new EruptWebApiRuntimeException("两次密码输入不一致");
            }
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Utils.digest(eruptUser.getPasswordA()));
            } else {
                eruptUser.setPassword(eruptUser.getPasswordA());
            }
        }
    }

    private void checkPostOrg(EruptUser eruptUser) {
        if (eruptUser.getEruptPost() != null && eruptUser.getEruptOrg() == null) {
            throw new EruptWebApiRuntimeException("选择岗位时，所属组织必填");
        }
    }

}
