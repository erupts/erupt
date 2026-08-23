package xyz.erupt.core.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptExceptionVo;
import xyz.erupt.core.view.R;

/**
 * @author YuePeng
 * date 2020-09-30
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ControllerAdvice(EruptConst.BASE_PACKAGE)
public class EruptExceptionAdvice {

    private static final String ERE = "erupt exception";

    @ExceptionHandler(EruptApiErrorTip.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public R<?> eruptApiErrorTip(EruptApiErrorTip e) {
        log.error(ERE, e);
        return e.r;
    }

    // Client disconnected (e.g. SSE stream closed by browser) — nothing can be written back
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void clientDisconnected(AsyncRequestNotUsableException e) {
        log.warn("Client disconnected: {}", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public EruptExceptionVo eruptException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error(ERE, e);
        // Response already committed (e.g. streaming) — writing a JSON body would fail
        if (response.isCommitted()) return null;
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new EruptExceptionVo(request.getServletPath(), response.getStatus(), ERE, e instanceof RuntimeException ? e.getMessage() : null);
    }

}
