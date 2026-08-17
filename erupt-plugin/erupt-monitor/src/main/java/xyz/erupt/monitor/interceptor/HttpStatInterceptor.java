package xyz.erupt.monitor.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import xyz.erupt.monitor.service.HttpStatService;

/**
 * @author YuePeng
 * date 2026/6/15
 */
@Component
public class HttpStatInterceptor implements HandlerInterceptor {

    private static final String START_ATTR = "HttpStatInterceptor.start";

    @Resource
    private HttpStatService httpStatService;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        Long start = (Long) request.getAttribute(START_ATTR);
        if (start == null) return;
        long elapsed = System.currentTimeMillis() - start;
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String uri = (pattern != null ? pattern : request.getRequestURI());
        httpStatService.record(request.getMethod() + " " + uri, elapsed, response.getStatus());
    }

}
