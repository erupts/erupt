package xyz.erupt.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.server.vo.Server;

/**
 * @author liyuepeng
 * @date 2021/1/23 21:36
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/server")
public class ServerController {

    public static final String ERUPT_OS = "/erupt_os";

    @RequestMapping(ERUPT_OS + "/info")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public Server info() {
        return new Server();
    }

    @RequestMapping(ERUPT_OS + "/gc")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public void gc() {
        System.gc();
    }

}
