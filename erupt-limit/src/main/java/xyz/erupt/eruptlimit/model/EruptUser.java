package xyz.erupt.eruptlimit.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.DataLength;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.AttachmentEnum;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.ImageType;
import xyz.erupt.core.handler.SimpleConditionHandler;
import xyz.erupt.eruptlimit.model.proxy.EruptUserProxy;
import lombok.Data;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Getter
@Setter
@Erupt(
        name = "用户",
        desc = "用户配置",
        dateProxy = EruptUserProxy.class
//        sorts = "account desc"
//        filter = @Filter(condition = "'id=@abc@'", conditionHandlers = {SimpleConditionHandler.class})
)
@Entity
@Table(name = "E_USER", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account")
})
public class EruptUser extends BaseModel {

    @Column(name = "ACCOUNT")
    @EruptField(
            views = @View(title = "用户名", sortable = true),
            edit = @Edit(title = "用户名", desc = "登录用户名", notNull = true)
    )
    private String account = "xxxxxx";

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "姓名", sortable = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(search = true))
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
                            falseText = "不加密",
                            defaultValue = true
                    ),
                    search = @Search(search = true)
            )
    )
    private Boolean isMD5;

    @Column(name = "AGE")
    @EruptField(
            views = @View(title = "年龄"),
            edit = @Edit(
                    title = "年龄",
                    search = @Search(search = true, vague = true)
            )
    )
    private Integer age;


    @Column(name = "HEAD_ICON")
    @EruptField(
            views = @View(title = "头像", viewType = ViewType.IMAGE),
            edit = @Edit(
                    title = "头像",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentEnum.IMAGE
                    )
            )
    )
    private String headIcon;

    @Column(name = "PWD")
    @EruptField(
            edit = @Edit(title = "密码", notNull = true)
    )
    private String password;

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
                            falseText = "锁定",
                            defaultValue = true
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
                    inputType = @InputType(type = InputEnum.TEXTAREA)
            )

    )
    private String whiteIp;

    @Lob
    @Column(name = "REMARK")
    @EruptField(
            edit = @Edit(
                    title = "备注",
                    inputType = @InputType(type = InputEnum.TEXTAREA)
            )
    )
    private String remark;


    @ManyToMany
    @JoinTable(
            name = "E_USER_ROLE",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    @EruptField(
            edit = @Edit(
                    title = "所属角色",
                    type = EditType.TAB,
                    tabType = @TabType
            )
    )
    private Set<EruptRole> roles;

}
