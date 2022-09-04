package xyz.erupt.job.model.data_proxy;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.job.model.EruptMail;

import javax.mail.internet.MimeMessage;
import javax.persistence.Transient;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2022/9/1 22:41
 */
@Service
public class EruptMailDataProxy implements DataProxy<EruptMail> {

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
