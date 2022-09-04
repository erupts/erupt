package xyz.erupt.job.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.constant.RegexConst;
import xyz.erupt.job.model.data_proxy.EruptMailDataProxy;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        name = "发送邮件",
        dataProxy = EruptMailDataProxy.class,
        power = @Power(edit = false),
        orderBy = "id desc"
)
@Entity
@Getter
@Setter
@Table(name = "e_job_mail")
@Component
public class EruptMail extends BaseModel {

    @EruptField(
            views = @View(title = "接收人"),
            edit = @Edit(title = "接收人", notNull = true, search = @Search(vague = true),
                    inputType = @InputType(fullSpan = true, regex = RegexConst.EMAIL_REGEX))
    )
    private String recipient;

    @EruptField(
            views = @View(title = "抄送人"),
            edit = @Edit(title = "抄送人", type = EditType.TAGS)
    )
    private String cc;

    @EruptField(
            views = @View(title = "主题"),
            edit = @Edit(title = "主题", notNull = true, search = @Search(vague = true), inputType = @InputType(fullSpan = true))
    )
    private String subject;

    @EruptField(
            views = @View(title = "状态"),
            edit = @Edit(title = "状态", boolType = @BoolType(trueText = "成功", falseText = "失败"), show = false)
    )
    private Boolean status;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(title = "内容", notNull = true, type = EditType.HTML_EDITOR)
    )
    private String content;

    @Column(length = 5000)
    private String errorInfo;

    @EruptField(
            views = @View(title = "发送时间")
    )
    private Date createTime;

    @EruptField(
            views = @View(title = "发送人")
    )
    @EruptSmartSkipSerialize
    private String createBy;


}
