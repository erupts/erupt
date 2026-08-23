package xyz.erupt.cloud.node.config;

import cn.hutool.core.codec.Base64Decoder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YuePeng
 * date 2018-12-20.
 */

@Configuration
@Component
public class EruptNodeInterceptor implements WebMvcConfigurer, AsyncHandlerInterceptor {

    @Resource
    private EruptNodeProp eruptNodeProp;

    // Requests currently being handled — consulted during graceful shutdown to drain in-flight work.
    private static final AtomicInteger IN_FLIGHT = new AtomicInteger();

    public static int inFlight() {
        return IN_FLIGHT.get();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler) {
        // The node erupt-api is server-to-server only; the cloud server forwards without an Origin
        // header. A present Origin means a browser is calling the node directly, which is prohibited.
        if (null != request.getHeader(HttpHeaders.ORIGIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (!eruptNodeProp.getAccessToken().equals(request.getHeader(CloudCommonConst.HEADER_ACCESS_TOKEN))) {
            throw new EruptWebApiRuntimeException("AccessToken incorrect");
        }
        MetaContext.registerToken(request.getHeader(EruptMutualConst.TOKEN));
        Optional.ofNullable(request.getHeader(EruptMutualConst.USER)).ifPresent(it ->
                MetaContext.register(GsonFactory.getGson().fromJson(Base64Decoder.decodeStr(it), MetaUser.class)));
        MetaContext.register(new MetaErupt(request.getHeader(EruptMutualConst.ERUPT)));
        IN_FLIGHT.incrementAndGet();
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        IN_FLIGHT.decrementAndGet();
        MetaContext.remove();
    }
}
