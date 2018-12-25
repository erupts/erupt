package xyz.erupt.eruptlimit.interceptor;

import xyz.erupt.base.model.EruptModel;
import xyz.erupt.service.CoreService;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liyuepeng on 12/5/18.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final String ERUPT_HEADER_KEY = "eruptKey";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String eruptKey = request.getHeader(ERUPT_HEADER_KEY);
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptKey);
        if (null == eruptModel) {
            return true;
        } else if (eruptModel.getErupt().loginUse()) {
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
