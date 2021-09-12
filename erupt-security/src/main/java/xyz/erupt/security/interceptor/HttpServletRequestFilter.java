package xyz.erupt.security.interceptor;

import org.springframework.web.filter.GenericFilterBean;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.model.ReqBody;
import xyz.erupt.security.tl.RequestBodyTL;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author YuePeng
 * date 2020-09-08
 */
public class HttpServletRequestFilter extends GenericFilterBean {

    private final EruptSecurityProp eruptSecurityProp;

    private static final String CONTENT_TYPE_JSON = "application/json";

    public HttpServletRequestFilter(EruptSecurityProp eruptSecurityProp) {
        this.eruptSecurityProp = eruptSecurityProp;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (eruptSecurityProp.isRecordOperateLog()) {
            ReqBody reqBody = new ReqBody();
            RequestBodyTL.set(reqBody);
            reqBody.setDate(System.currentTimeMillis());
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                if (request.getServletPath().contains(EruptRestPath.ERUPT_API)) {
                    if (null != request.getContentType() && CONTENT_TYPE_JSON.equals(request.getContentType())) {
                        EruptRequestWrapper eruptRequestWrapper = new EruptRequestWrapper(request);
                        reqBody.setBody(eruptRequestWrapper.getBody());
                        filterChain.doFilter(eruptRequestWrapper, servletResponse);
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
