package xyz.erupt.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptApiModel;

/**
 * @author liyuepeng
 * @date 2020-04-10
 */
@ControllerAdvice
public class EruptApiExceptionHandler {

    @ExceptionHandler(EruptApiErrorTip.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public EruptApiModel handleInvalidRequestError(EruptApiErrorTip e) {
        e.eruptApiModel.setErrorIntercept(false);
        return e.eruptApiModel;
    }

}
