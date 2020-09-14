//package xyz.erupt.auth.interceptor;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import xyz.erupt.auth.config.EruptAuthConfig;
//import xyz.erupt.core.constant.RestPath;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
///**
// * @author liyuepeng
// * @date 2020-09-08
// */
//@Component
//@WebFilter(urlPatterns = RestPath.ERUPT_API)
//@Order(10000)
//public class HttpServletRequestFilter implements Filter {
//
//    @Autowired
//    private EruptAuthConfig eruptAuthConfig;
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        if (eruptAuthConfig.isRecordOperateLog()) {
//            ServletRequest requestWrapper = null;
//            if (servletRequest instanceof HttpServletRequest) {
//                requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);
//                servletRequest.setAttribute(LoginInterceptor.REQ_BODY, ((RequestWrapper) requestWrapper).getBody());
//            }
//            if (null == requestWrapper) {
//                filterChain.doFilter(servletRequest, servletResponse);
//            } else {
//                filterChain.doFilter(requestWrapper, servletResponse);
//            }
//        } else {
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
//
//    }
//
//}
