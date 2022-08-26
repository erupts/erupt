package xyz.erupt.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.constant.RegexConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.model.BaseModel;

import javax.mail.internet.MimeMessage;
import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        name = "发送邮件",
        dataProxy = EruptMail.class,
        power = @Power(edit = false),
        orderBy = "id desc"
)
@Entity
@Getter
@Setter
@Table(name = "e_job_mail")
@Component
public class EruptMail extends BaseModel implements DataProxy<EruptMail> {

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

    @Transient
    @Autowired(required = false)
    private JavaMailSenderImpl javaMailSender;

    @SneakyThrows
    @Override
    public void beforeAdd(EruptMail eruptMail) {
        Erupts.requireNonNull(javaMailSender, "Sending mailbox not configured");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        eruptMail.setCreateBy(MetaContext.getUser().getName());
        eruptMail.setCreateTime(new Date());
        helper.setSubject(eruptMail.getSubject());
        helper.setTo(eruptMail.getRecipient());
        helper.setFrom(Objects.requireNonNull(javaMailSender.getUsername()));
        if (StringUtils.isNotBlank(eruptMail.getCc())) helper.setCc(eruptMail.getCc().split("\\|"));
        helper.setText(eruptMail.getContent(), true);
        try {
            javaMailSender.send(mimeMessage);
            eruptMail.setStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
            eruptMail.setStatus(false);
            Optional.ofNullable(e.toString()).ifPresent(it -> eruptMail.setErrorInfo(it.substring(0, 5000)));
        }
    }
}
