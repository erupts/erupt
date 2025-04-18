package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.constant.RegexConst;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.looker.LookerSelf;
import xyz.erupt.upms.model.filter.EruptMenuViewFilter;
import xyz.erupt.upms.model.input.ResetPassword;
import xyz.erupt.upms.model.input.ResetPasswordExec;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user")
@Erupt(
        name = "用户管理",
        dataProxy = EruptUserDataProxy.class,
        linkTree = @LinkTree(field = "eruptOrg"),
        orderBy = "EruptUser.id",
        rowOperation = @RowOperation(title = "重置密码",
                icon = "fa fa-refresh",
                mode = RowOperation.Mode.SINGLE,
                eruptClass = ResetPassword.class,
                operationHandler = ResetPasswordExec.class)
)
@EruptI18n
@Getter
@Setter
public class EruptUser extends LookerSelf {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
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
            views = @View(title = "账户状态", sortable = true),
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
            edit = @Edit(title = "手机号码", search = @Search(vague = true), inputType = @InputType(regex = RegexConst.PHONE_REGEX))
    )
    private String phone;

    @EruptField(
            edit = @Edit(title = "邮箱", search = @Search(vague = true), inputType = @InputType(regex = RegexConst.EMAIL_REGEX))
    )
    private String email;

    @EruptField(
            views = @View(title = "超管用户", sortable = true),
            edit = @Edit(
                    title = "超管用户", notNull = true, search = @Search(vague = true)
            )
    )
    private Boolean isAdmin = false;

    @ManyToOne
    @EruptField(
            views = @View(title = "首页菜单", column = "name"),
            edit = @Edit(
                    title = "首页菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id"),
                    filter = @Filter(conditionHandler = EruptMenuViewFilter.class)
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
            edit = @Edit(title = "岗位", type = EditType.REFERENCE_TREE, search = @Search)
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
            edit = @Edit(title = "密码", readonly = @Readonly(add = false))
    )
    private String passwordA;

    @Transient
    @EruptField(
            edit = @Edit(title = "确认密码", readonly = @Readonly(add = false))
    )
    private String passwordB;

    @EruptField(
            views = @View(title = "重置密码时间", width = "130px", sortable = true)
    )
    private Date resetPwdTime;

    @EruptField(
            edit = @Edit(
                    title = "md5加密", type = EditType.BOOLEAN, notNull = true,
                    readonly = @Readonly(add = false),
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    )
            )
    )
    private Boolean isMd5 = true;

    @EruptField(
            views = @View(title = "账号失效时间", sortable = true),
            edit = @Edit(title = "账号失效时间")
    )
    private Date expireDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "e_upms_user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @OrderBy
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

    public EruptUser() {
    }

    public EruptUser(Long id) {
        this.setId(id);
    }

    public MetaUserinfo toMetaUser(){
        MetaUserinfo metaUserinfo = new MetaUserinfo();
        metaUserinfo.setId(this.getId());
        metaUserinfo.setSuperAdmin(this.getIsAdmin());
        metaUserinfo.setAccount(this.getAccount());
        metaUserinfo.setUsername(this.getName());
        Optional.ofNullable(this.getRoles()).ifPresent(it-> metaUserinfo.setRoles(it.stream().map(EruptRole::getCode).collect(Collectors.toList())));
        Optional.ofNullable(this.getEruptPost()).ifPresent(it -> metaUserinfo.setPost(it.getCode()));
        Optional.ofNullable(this.getEruptOrg()).ifPresent(it -> metaUserinfo.setOrg(it.getCode()));
        return metaUserinfo;
    }

}
