package xyz.erupt.auth.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.core.constant.RestPath;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author liyuepeng
 * @date 2020-09-08
 */
@Component
@WebFilter(urlPatterns = RestPath.ERUPT_API)
public class HttpServletRequestFilter implements Filter {

    @Autowired
    private EruptAuthConfig eruptAuthConfig;

    private static final String CONTENT_TYPE_JSON = "application/json";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (eruptAuthConfig.isRecordOperateLog()) {
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                if (request.getServletPath().contains(RestPath.ERUPT_API)) {
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
