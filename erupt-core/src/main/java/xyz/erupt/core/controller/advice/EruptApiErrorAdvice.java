package xyz.erupt.core.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptApiModel;

/**
 * @author YuePeng
 * date 2023/11/7 22:42
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EruptApiErrorAdvice {

    private static final String API_ERROR = "erupt web api error";

    @ExceptionHandler(EruptApiErrorTip.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public EruptApiModel eruptApiErrorTip(EruptApiErrorTip e) {
        log.error(API_ERROR, e);
        e.eruptApiModel.setErrorIntercept(false);
        return e.eruptApiModel;
    }

}
