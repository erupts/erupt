package xyz.erupt;

import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.controller.EruptUserController;
import xyz.erupt.upms.prop.EruptUpmsProp;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EruptApplicationTests {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserController eruptUserController;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    public String login() {
        LoginModel login = eruptUserController.login(eruptUpmsProp.getDefaultAccount(),
                MD5Util.digest(MD5Util.digest(eruptUpmsProp.getDefaultPassword()) + eruptUpmsProp.getDefaultAccount()), null, null);
        if (login.isPass()) {
            return login.getToken();
        } else {
            throw new RuntimeException(login.getReason());
        }
    }


}
