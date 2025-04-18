package xyz.erupt.security.interceptor;

import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.GenericFilterBean;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.model.ReqBody;
import xyz.erupt.security.tl.RequestBodyTL;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
                        HttpServletRequestFilter.EruptRequestWrapper eruptRequestWrapper =
                                new EruptRequestWrapper(request);
                        reqBody.setBody(eruptRequestWrapper.getBody());
                        filterChain.doFilter(eruptRequestWrapper, servletResponse);
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private static class EruptRequestWrapper extends HttpServletRequestWrapper {

        private final String body;

        @SneakyThrows
        EruptRequestWrapper(HttpServletRequest request) {
            super(request);
            body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };

        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
        }

        String getBody() {
            return this.body;
        }

    }
}
