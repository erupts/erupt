package xyz.erupt.eruptlimit.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;
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

    private static final String URL_ERUPT_PARAM_TOKEN = "_token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        String eruptName = request.getHeader(ERUPT_HEADER_KEY);
        {
            if (StringUtils.isBlank(token)) {
                //使用参数的形式携带token等信息
                if (request.getServletPath().contains(RestPath.ERUPT_EXCEL)) {
                    token = request.getParameter(URL_ERUPT_PARAM_TOKEN);
                    eruptName = request.getParameter(URL_ERUPT_PARAM_KEY);
                }
            }
        }
        if (null == token || !loginService.verifyToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        String path = request.getServletPath();
        //权限校验
        if (path.startsWith(RestPath.ERUPT_API)) {
            EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
            if (null == eruptModel) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
            if (!path.contains(eruptName) || !loginService.verifyMenuLimit(token, path, eruptModel)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
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
