package xyz.erupt.cloud.node.config;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Verifies {@link CloudSignature} on server -> node calls. A request carrying a signature must carry a valid
 * one; {@link EruptNodeInterceptor} then accepts it without the plain access token. The body is buffered for
 * the digest and handed on unchanged (a multipart body is not covered by the signature).
 *
 * @author YuePeng
 * date 2026/10/14
 */
@Component
public class NodeSignatureFilter extends OncePerRequestFilter {

    // request attribute set once the signature checked out
    public static final String VERIFIED = NodeSignatureFilter.class.getName() + ".verified";

    // request attribute: the checks the server signed as enforced (Erupt-Enforced), set only once verified
    private static final String ENFORCED = NodeSignatureFilter.class.getName() + ".enforced";

    @Resource
    private EruptNodeProp eruptNodeProp;

    /**
     * Whether the server, in a verified signature, vouched for {@code check} on the current request.
     */
    public static boolean enforced(CloudSignature.Enforced check) {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && attributes.getRequest().getAttribute(ENFORCED) instanceof Set<?> enforced && enforced.contains(check);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().substring(request.getContextPath().length()).startsWith(EruptRestPath.ERUPT_API + "/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String signature = request.getHeader(CloudSignature.HEADER_SIGNATURE);
        if (null == signature) {
            if (eruptNodeProp.isRequireSignature()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        boolean multipart = null != request.getContentType() && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE);
        HttpServletRequest target = request;
        byte[] body = null;
        if (!multipart) {
            body = StreamUtils.copyToByteArray(request.getInputStream());
            target = new CachedBodyRequest(request, body);
        }
        String path = request.getRequestURI().substring(request.getContextPath().length())
                + (null == request.getQueryString() ? "" : "?" + request.getQueryString());
        if (!CloudSignature.verify(eruptNodeProp.getAccessToken(), request.getMethod(), path,
                request.getHeader(CloudSignature.HEADER_TIMESTAMP), request.getHeader(CloudSignature.HEADER_ENFORCED),
                request.getHeader(EruptMutualConst.USER), body, signature, System.currentTimeMillis() / 1000)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        target.setAttribute(VERIFIED, Boolean.TRUE);
        target.setAttribute(ENFORCED, CloudSignature.Enforced.parse(request.getHeader(CloudSignature.HEADER_ENFORCED)));
        chain.doFilter(target, response);
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return in.read();
                }

                @Override
                public int read(@NonNull byte[] b, int off, int len) {
                    return in.read(b, off, len);
                }

                @Override
                public boolean isFinished() {
                    return in.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public java.io.BufferedReader getReader() {
            return new java.io.BufferedReader(new java.io.InputStreamReader(getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
        }

    }

}
