package xyz.erupt.core.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptApiModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-09-30
 */
@ControllerAdvice
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EruptExceptionAdvice {
    public static final String API_ERROR = "erupt web api error";
    private static final String ERE = "erupt exception";

    @ExceptionHandler(EruptApiErrorTip.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public EruptApiModel eruptApiErrorTip(EruptApiErrorTip e) {
        log.error(API_ERROR, e);
        e.eruptApiModel.setErrorIntercept(false);
        return e.eruptApiModel;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Map<String, Object> eruptRuntimeException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error(ERE, e);
        Map<String, Object> map = new HashMap<>();
        map.put("error", ERE);
        map.put("timestamp", new Date());
        map.put("path", request.getServletPath());
        map.put("status", response.getStatus());
        if (e instanceof RuntimeException) {
            map.put("message", e.getMessage());
        }
        return map;
    }

}
