package xyz.erupt.eruptlimit.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.core.constant.HttpStatus;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.InitService;
import xyz.erupt.eruptlimit.service.LoginService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liyuepeng on 12/5/18.
 */
@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    public LoginService loginService;

    private static final String ERUPT_HEADER_KEY = "erupt";

    private static final String URL_ERUPT_PARAM_KEY = "_erupt";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (null == token || !loginService.verifyToken(token)) {
            response.setStatus(HttpStatus.NO_LOGIN.code);
            return false;
        }
        String path = request.getServletPath();
        //权限校验
        if (path.startsWith(RestPath.ERUPT_API)) {
            String eruptName = request.getHeader(ERUPT_HEADER_KEY);
            if (StringUtils.isBlank(eruptName)) {
                eruptName = request.getAttribute(URL_ERUPT_PARAM_KEY).toString();
            }
            EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
            if (null == eruptModel) {
                response.setStatus(HttpStatus.NOT_FOUNT.code);
                return false;
            }
            if (!path.contains(eruptName) || !loginService.verifyMenuLimit(token, path, eruptModel)) {
                response.setStatus(HttpStatus.NO_RIGHT.code);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
