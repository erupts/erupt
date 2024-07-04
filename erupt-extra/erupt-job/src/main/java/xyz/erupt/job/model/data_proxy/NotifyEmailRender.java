package xyz.erupt.job.model.data_proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.expr.ExprBool;

/**
 * @author YuePeng
 * date 2024/6/11 21:08
 */
@Service
public class NotifyEmailRender implements ExprBool.ExprHandler {

    @Autowired(required = false)
    private MailProperties mailProperties;

    @Override
    public boolean handler(boolean expr, String[] params) {
        return null != mailProperties;
    }

}
