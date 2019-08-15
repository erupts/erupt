package xyz.erupt.auth.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.AttachmentEnum;
import xyz.erupt.eruptcommon.util.MD5Utils;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_USER", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account")
})
@Erupt(
        name = "用户",
        desc = "用户配置",
        dateProxy = EruptUser.class
)
@Getter
@Setter
public class EruptUser extends BaseModel implements DataProxy<EruptUser> {

    @Column(name = "ACCOUNT")
    @EruptField(
            views = @View(title = "用户名", sortable = true),
            edit = @Edit(title = "用户名", desc = "登录用户名", notNull = true)
    )
    private String account;

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "姓名", sortable = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(value = true, vague = true))
    )
    private String name;

    @Column(name = "IS_MD5")
    @EruptField(
            views = @View(title = "md5加密"),
            edit = @Edit(
                    title = "md5加密",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    ),
                    search = @Search(true)
            )
    )
    private Boolean isMD5;


    @Column(name = "HEAD_ICON")
    @EruptField(
            views = @View(title = "头像", viewType = ViewType.IMAGE, export = false),
            edit = @Edit(
                    title = "头像",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentEnum.IMAGE,
                            maxLimit = 5
                    )
            )
    )
    private String headIcon;

    @Column(name = "PWD")
    @EruptField(
            edit = @Edit(title = "密码", notNull = true)
    )
    private String password = "pwd";

    @Transient
    @EruptField(
            edit = @Edit(title = "确认密码")
    )
    private String password2;

    @Column(name = "STATUS")
    @EruptField(
            views = @View(title = "状态"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(
                            trueText = "激活",
                            falseText = "锁定"
                    )
            )
    )
    private Boolean status;

    @Lob
    @Column(name = "WHITE_IP")
    @EruptField(
            edit = @Edit(
                    title = "ip白名单",
                    desc = "ip与ip之间使用换行符间隔，不填表示不鉴权",
                    type = EditType.TEXTAREA
            )

    )
    private String whiteIp;

    @Lob
    @Column(name = "REMARK")
    @EruptField(
            edit = @Edit(
                    title = "备注",
                    type = EditType.TEXTAREA,
                    search = @Search(value = true, vague = true)
            )
    )
    private String remark = "999999";


    @ManyToMany
    @JoinTable(
            name = "E_USER_ROLE",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    @EruptField(
            edit = @Edit(
                    title = "所属角色",
                    type = EditType.TAB_TREE
            )
    )
    private Set<EruptRole> roles;

    @Column(name = "IS_ADMIN")
    private Boolean isAdmin;

    @Override
    public BoolAndReason beforeAdd(EruptUser eruptUser) {
        if (eruptUser.getPassword().equals(eruptUser.getPassword2())) {
            if (eruptUser.getIsMD5()) {
                eruptUser.setPassword(MD5Utils.digest(eruptUser.getPassword()));
            }
            return new BoolAndReason(true, null);
        } else {
            return new BoolAndReason(false, "两次密码输入不一致");
        }
    }

}
