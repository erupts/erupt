package xyz.erupt.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.constant.AnnotationConst;
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

import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        name = "Mail Management",
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
            views = @View(title = "Recipient"),
            edit = @Edit(title = "Recipient", notNull = true, search = @Search(vague = true),
                    inputType = @InputType(fullSpan = true, regex = RegexConst.EMAIL_REGEX))
    )
    private String recipient;

    @EruptField(
            views = @View(title = "CC"),
            edit = @Edit(title = "CC", type = EditType.TAGS)
    )
    private String cc;

    @EruptField(
            views = @View(title = "Subject"),
            edit = @Edit(title = "Subject", notNull = true, search = @Search(vague = true), inputType = @InputType(fullSpan = true))
    )
    private String subject;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", boolType = @BoolType(trueText = "Success", falseText = "Failure"), show = false)
    )
    private Boolean status;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Content"),
            edit = @Edit(title = "Content", notNull = true, type = EditType.HTML_EDITOR)
    )
    private String content;

    @Column(length = 5000)
    private String errorInfo;

    @EruptField(
            views = @View(title = "Sent Time")
    )
    private Date createTime;

    @EruptField(
            views = @View(title = "Sender")
    )
    @EruptSmartSkipSerialize
    private String createBy;


}
