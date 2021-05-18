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
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.job.constant.JobConst;
import xyz.erupt.upms.model.base.HyperModel;

import javax.mail.internet.MimeMessage;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2019-12-26
 */
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
public class EruptMail extends HyperModel implements DataProxy<EruptMail> {

    @EruptField(
            views = @View(title = "接收人"),
            edit = @Edit(title = "接收人", notNull = true, search = @Search,
                    inputType = @InputType(fullSpan = true, regex = JobConst.EMAIL_REGEX))
    )
    private String recipient;

    @EruptField(
            views = @View(title = "抄送人", template = JobConst.BEAUTIFUL_TAG),
            edit = @Edit(title = "抄送人", type = EditType.TAGS)
    )
    private String cc;

    @EruptField(
            views = @View(title = "主题"),
            edit = @Edit(title = "主题", notNull = true, search = @Search, inputType = @InputType(fullSpan = true))
    )
    private String subject;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(title = "内容", notNull = true, type = EditType.HTML_EDITOR)
    )
    private String content;

    @EruptField(
            views = @View(title = "发送时间")
    )
    private Date createTime;

    @Transient
    @Autowired(required = false)
    private JavaMailSenderImpl javaMailSender;

    @SneakyThrows
    @Override
    public void beforeAdd(EruptMail eruptMail) {
        if (null == javaMailSender) {
            throw new EruptApiErrorTip("Sending mailbox not configured");
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setSubject(eruptMail.getSubject());
        helper.setTo(eruptMail.getRecipient());
        helper.setFrom(Objects.requireNonNull(javaMailSender.getUsername()));
        if (StringUtils.isNotBlank(eruptMail.getCc())) {
            helper.setCc(eruptMail.getCc().split("\\|"));
        }
        helper.setText(eruptMail.getContent(), true);
        javaMailSender.send(mimeMessage);
    }
}
