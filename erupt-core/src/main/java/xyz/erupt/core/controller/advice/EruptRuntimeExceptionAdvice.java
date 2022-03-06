package xyz.erupt.core.controller.advice;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.erupt.core.constant.EruptConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 向前端展示所有erupt运行时异常
 *
 * @author YuePeng
 * date 2022/1/9 01:55
 */
@ControllerAdvice(EruptConst.BASE_PACKAGE)
@Order
public class EruptRuntimeExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> eruptRuntimeException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Map<String, Object> map = new HashMap<>();
        map.put("error", "erupt runtime exception");
        map.put("message", e.getMessage());
        map.put("timestamp", new Date());
        map.put("path", request.getServletPath());
        map.put("status", response.getStatus());
        e.printStackTrace();
        return map;
    }
}
