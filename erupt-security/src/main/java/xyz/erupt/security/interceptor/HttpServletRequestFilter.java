package xyz.erupt.security.interceptor;

import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.config.EruptSecurityProp;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author YuePeng
 * date 2020-09-08
 */
@Component
public class HttpServletRequestFilter implements Filter {

    @Resource
    private EruptSecurityProp eruptSecurityProp;

    private static final String CONTENT_TYPE_JSON = "application/json";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (eruptSecurityProp.isRecordOperateLog()) {
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                if (request.getServletPath().contains(EruptRestPath.ERUPT_API)) {
                    if (null != request.getContentType() && CONTENT_TYPE_JSON.equals(request.getContentType())) {
                        RequestWrapper requestWrapper = new RequestWrapper(request);
                        servletRequest.setAttribute(LoginInterceptor.REQ_BODY, requestWrapper.getBody());
                        filterChain.doFilter(requestWrapper, servletResponse);
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
