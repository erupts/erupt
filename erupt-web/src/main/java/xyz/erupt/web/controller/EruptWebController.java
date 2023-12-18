package xyz.erupt.web.controller;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.web.EruptWebConfigurer;
import xyz.erupt.web.base.LoginModel;
import xyz.erupt.web.prop.EruptAppProp;
import xyz.erupt.web.vo.EruptMenuVo;
import xyz.erupt.web.vo.EruptUserinfoVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@RestController
public class EruptWebController {

    @Resource
    private EruptAppProp eruptAppProp;
    @Resource
    private EruptWebConfigurer webConfigurer;

    private final String passport;

    public EruptWebController() throws IOException {
        try (InputStream input = EruptWebController.class.getClassLoader().getResourceAsStream("public/index.html")) {
            if (null == input) throw new RuntimeException("erupt-web resources not found");
            this.passport = StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        }
    }

    @GetMapping(value = {"/", "/index.html"}, produces = {"text/html;charset=utf-8"})
    public String index(HttpServletResponse response) {
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
        return passport;
    }

    @GetMapping(EruptRestPath.ERUPT_API + "/erupt-app")
    public EruptAppProp eruptApp() {
        eruptAppProp.setHash(this.hashCode());
        eruptAppProp.setResetPwd(false);
        eruptAppProp.setVersion(EruptInformation.getEruptVersion());
        return eruptAppProp;
    }

    /**
     * 登录
     *
     * @param account        用户名
     * @param pwd            密码
     */
    @SneakyThrows
    @GetMapping(EruptRestPath.ERUPT_API + "/login")
    public LoginModel login(@RequestParam String account, @RequestParam String pwd, HttpServletRequest request) {
        return webConfigurer.login(account, pwd, request);
    }

    //登出
    @GetMapping(EruptRestPath.ERUPT_API + "/logout")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel logout(HttpServletRequest request) {
        return webConfigurer.logout(request);
    }

    //用户信息
    @GetMapping(EruptRestPath.ERUPT_API + "/userinfo")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptUserinfoVo userinfo(HttpServletRequest request) {
        return webConfigurer.getUserinfo(request);
    }

    //获取菜单列表
    @GetMapping(EruptRestPath.ERUPT_API + "/menu")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public List<EruptMenuVo> getMenu(HttpServletRequest request) {
        return webConfigurer.getMenus(request);
    }

}
