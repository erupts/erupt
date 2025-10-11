package xyz.erupt.job.model.data_proxy;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.job.model.EruptMail;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2022/9/1 22:41
 */
@Slf4j
@Service
public class EruptMailDataProxy implements DataProxy<EruptMail> {

    private JavaMailSenderImpl javaMailSender;

    @Autowired(required = false)
    public void setJavaMailSender(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

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
            log.error("mail send error", e);
            eruptMail.setStatus(false);
            Optional.ofNullable(e.toString()).ifPresent(it -> eruptMail.setErrorInfo(it.substring(0, 5000)));
        }
    }

}
