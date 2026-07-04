package xyz.erupt.security.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.GenericFilterBean;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.model.ReqBody;
import xyz.erupt.security.tl.RequestBodyTL;

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
        if (!eruptSecurityProp.isRecordOperateLog()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // finally guarantees ThreadLocal cleanup on every path (exceptions, non-recorded requests)
        try {
            ReqBody reqBody = new ReqBody();
            RequestBodyTL.set(reqBody);
            reqBody.setDate(System.currentTimeMillis());
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                if (request.getServletPath().contains(EruptRestPath.ERUPT_API)
                        && null != request.getContentType() && CONTENT_TYPE_JSON.equals(request.getContentType())) {
                    // Only buffer bodies with a declared size within the limit;
                    // chunked (-1) or oversized bodies pass through unbuffered to prevent pre-auth memory exhaustion
                    long contentLength = request.getContentLengthLong();
                    if (contentLength >= 0 && contentLength <= eruptSecurityProp.getRecordOperateLogMaxBodySize()) {
                        EruptRequestWrapper eruptRequestWrapper = new EruptRequestWrapper(request);
                        reqBody.setBody(eruptRequestWrapper.getBody());
                        filterChain.doFilter(eruptRequestWrapper, servletResponse);
                        return;
                    }
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            RequestBodyTL.remove();
        }
    }

    private static class EruptRequestWrapper extends HttpServletRequestWrapper {

        // Kept as bytes so replaying the stream does not allocate another full copy
        private final byte[] body;

        @SneakyThrows
        EruptRequestWrapper(HttpServletRequest request) {
            super(request);
            body = StreamUtils.copyToByteArray(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
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
            return new String(this.body, StandardCharsets.UTF_8);
        }

    }
}
