package xyz.erupt.web;

import xyz.erupt.web.base.LoginModel;
import xyz.erupt.web.vo.EruptMenuVo;
import xyz.erupt.web.vo.EruptUserinfoVo;
import xyz.erupt.core.view.EruptApiModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * @author fanhang
 */
public interface EruptWebConfigurer {
    /**
     * 登录
     * @param account account
     * @param pwd pwd
     * @param request request
     * @return LoginModel
     */
    LoginModel login(String account, String pwd, HttpServletRequest request);

    /**
     * 登出
     * @param request request
     * @return EruptApiModel
     */
    default EruptApiModel logout(HttpServletRequest request) {
        return EruptApiModel.successApi();
    }

    /**
     * 获取当前用户信息
     * @param request request
     * @return EruptUserinfoVo
     */
    EruptUserinfoVo getUserinfo(HttpServletRequest request);

    /**
     * 获取当前用户的菜单
     * @param request request
     * @return List<EruptMenuVo>
     */
    default List<EruptMenuVo> getMenus(HttpServletRequest request) {
        return Collections.emptyList();
    }
}
