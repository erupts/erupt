package xyz.erupt.upms.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptUserService;

/**
 * Permission and app settings
 *
 * @apiNote Permission probe and application settings.
 *
 * @author YuePeng
 * date 2022/3/9 19:18
 */
@RestController
@AllArgsConstructor
public class EruptUPMSController {

    private final EruptUserService eruptUserService;

    private final EruptAppProp eruptAppProp;

    /**
     * Whether the current user holds a menu permission.
     *
     * @param menuValue Menu value, for example an erupt class name or a custom code
     * @return true when the menu is granted
     */
    @GetMapping(EruptRestPath.ERUPT_CODE_PERMISSION + "/{value}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public boolean eruptPermission(@PathVariable("value") String menuValue) {
        return null != eruptUserService.getEruptMenuByValue(menuValue);
    }

    /**
     * Public application settings.
     *
     * @apiNote Read by the frontend before login: title, login options, password transfer encoding, version and the attachment file domain. No session needed.
     * @return application properties
     */
    @GetMapping(EruptRestPath.ERUPT_API + "/erupt-app")
    public EruptAppProp eruptApp() {
        eruptAppProp.setHash(this.hashCode());
        eruptAppProp.setVersion(EruptInformation.getEruptVersion());
        AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
        eruptAppProp.setFileDomain(null == attachmentProxy ? null : attachmentProxy.fileDomain());
        return eruptAppProp;
    }

}
