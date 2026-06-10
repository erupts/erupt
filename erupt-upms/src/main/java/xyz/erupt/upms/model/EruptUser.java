package xyz.erupt.upms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.core.constant.RegexConst;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.looker.LookerSelf;
import xyz.erupt.upms.model.data_proxy.EruptOrgFetchHandler;
import xyz.erupt.upms.model.data_proxy.EruptUserDataProxy;
import xyz.erupt.upms.model.filter.EruptMenuViewFilter;
import xyz.erupt.upms.model.input.ResetPassword;
import xyz.erupt.upms.model.input.ResetPasswordExec;

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
        name = "User Management",
        dataProxy = EruptUserDataProxy.class,
        linkTree = @LinkTree(field = "eruptOrg"),
        orderBy = "EruptUser.id",
        layout = @Layout(tableLeftFixed = 1),
        rowOperation = @RowOperation(title = "Reset Password",
                icon = "fa fa-refresh",
                mode = RowOperation.Mode.SINGLE,
                eruptClass = ResetPassword.class,
                operationHandler = ResetPasswordExec.class)
)
@EruptI18n
@Getter
@Setter
public class EruptUser extends LookerSelf {

    @Column(length = 1023)
    private String avatar;

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "Account", sortable = true),
            edit = @Edit(title = "Account", desc = "Login account", notNull = true, search = @Search)
    )
    private String account;

    @EruptField(
            views = @View(title = "Full Name", sortable = true),
            edit = @Edit(title = "Full Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "Account Status", sortable = true),
            edit = @Edit(
                    title = "Account Status",
                    search = @Search,
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "Activate",
                            falseText = "Locked"
                    )
            )
    )
    private Boolean status = true;

    @EruptField(
            edit = @Edit(title = "Phone", search = @Search, inputType = @InputType(regex = RegexConst.PHONE_REGEX))
    )
    private String phone;

    @EruptField(
            edit = @Edit(title = "Email", search = @Search, inputType = @InputType(regex = RegexConst.EMAIL_REGEX))
    )
    private String email;

    @EruptField(
            views = @View(title = "Admin User", sortable = true),
            edit = @Edit(
                    title = "Admin User", notNull = true, search = @Search
            )
    )
    private Boolean isAdmin = false;

    @ManyToOne
    @EruptField(
            views = @View(title = "Home Menu", column = "name"),
            edit = @Edit(
                    title = "Home Menu",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id"),
                    filter = @Filter(conditionHandler = EruptMenuViewFilter.class)
            )
    )
    private EruptMenu eruptMenu;

    @ManyToOne
    @EruptField(
            views = @View(title = "Org", column = "name"),
            edit = @Edit(title = "Org", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id"))
    )
    private EruptOrg eruptOrg;

    @ManyToOne
    @EruptField(
            views = @View(title = "Post", column = "name"),
            edit = @Edit(title = "Post", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private EruptPost eruptPost;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "e_upms_user_org_head", joinColumns = @JoinColumn(name = "id"))
    @EruptField(
            views = @View(title = "Responsible Org", column = "name"),
            edit = @Edit(title = "Responsible Org", type = EditType.MULTI_CHOICE, multiChoiceType =
            @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = EruptOrgFetchHandler.class))
    )
    private Set<Long> headOrg;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "e_upms_user_org_division", joinColumns = @JoinColumn(name = "id"))
    @EruptField(
            views = @View(title = "Supervising Org", column = "name"),
            edit = @Edit(title = "Supervising Org", type = EditType.MULTI_CHOICE, multiChoiceType =
            @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = EruptOrgFetchHandler.class))
    )
    private Set<Long> divisionOrg;

    @Transient
    @EruptField(
            edit = @Edit(title = "Password", type = EditType.GROUP,
                    groupType = @GroupType(fields = {"passwordA", "passwordB", "isMd5"})
            )
    )
    private String pwdGroup;

    private String password;

    @Transient
    @EruptField(
            edit = @Edit(title = "Password", readonly = @Readonly(add = false), type = EditType.PASSWORD)
    )
    private String passwordA;

    @Transient
    @EruptField(
            edit = @Edit(title = "Confirm Password", readonly = @Readonly(add = false), type = EditType.PASSWORD)
    )
    private String passwordB;

    @EruptField(
            views = @View(title = "Reset Pwd Time", width = "130px", sortable = true)
    )
    private Date resetPwdTime;

    @EruptField(
            edit = @Edit(
                    title = "Encrypt", type = EditType.BOOLEAN, notNull = true,
                    readonly = @Readonly(add = false),
                    boolType = @BoolType(
                            trueText = "Encrypt",
                            falseText = "No Encrypt"
                    )
            )
    )
    private Boolean isMd5 = true;

    @Column(length = 64)
    private String salt;

    @Column(length = 20)
    private String encryptType;

    @EruptField(
            views = @View(title = "Account Expiry", sortable = true),
            edit = @Edit(title = "Account Expiry")
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
            views = @View(title = "Role"),
            edit = @Edit(
                    title = "Role",
                    type = EditType.CHECKBOX
            )
    )
    private Set<EruptRole> roles;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "IP Whitelist",
                    desc = "Separate IPs with newline; leave empty for no auth check",
                    type = EditType.TEXTAREA
            )
    )
    private String whiteIp;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "remark",
                    type = EditType.TEXTAREA
            )
    )
    private String remark;

    public EruptUser() {
    }

    public EruptUser(Long id) {
        this.setId(id);
    }

    public MetaUserinfo toMetaUser() {
        MetaUserinfo metaUserinfo = new MetaUserinfo();
        metaUserinfo.setId(this.getId());
        metaUserinfo.setSuperAdmin(this.getIsAdmin());
        metaUserinfo.setAccount(this.getAccount());
        metaUserinfo.setUsername(this.getName());
        Optional.ofNullable(this.getRoles()).ifPresent(it -> metaUserinfo.setRoles(it.stream().map(EruptRole::getCode).collect(Collectors.toList())));
        Optional.ofNullable(this.getEruptPost()).ifPresent(it -> metaUserinfo.setPost(it.getCode()));
        Optional.ofNullable(this.getEruptOrg()).ifPresent(it -> metaUserinfo.setOrg(it.getCode()));
        return metaUserinfo;
    }

}