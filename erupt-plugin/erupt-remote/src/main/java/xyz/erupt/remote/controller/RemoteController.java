package xyz.erupt.remote.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.service.RemoteTicketService;
import xyz.erupt.remote.util.RemoteCrypto;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.service.EruptContextService;

import java.util.HashMap;
import java.util.Map;

/**
 * Issues one-time session tickets for the remote views.
 * Every endpoint requires the {@code RemoteHost} menu, enforced by {@link EruptMenuAuth}.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/remote")
public class RemoteController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RemoteTicketService ticketService;

    @Resource
    private RemoteCrypto remoteCrypto;

    @Resource
    private EruptContextService eruptContextService;

    @GetMapping("/ticket/{id}")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<Map<String, Object>> ticket(@PathVariable("id") Long id) {
        RemoteHost host = eruptDao.find(RemoteHost.class, id);
        Erupts.requireNonNull(host, I18nTranslate.$translate("remote.host_not_found"));
        Erupts.requireTrue(Boolean.TRUE.equals(host.getEnabled()), I18nTranslate.$translate("remote.host_disabled"));
        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticketService.issue(host.getId(), eruptContextService.getCurrentToken()));
        result.put("name", host.getName());
        result.put("protocol", host.getProtocol());
        // Tells the page whether the server will answer authentication on its behalf
        result.put("passwordManaged", remoteCrypto.decrypt(host.getPassword()) != null);
        return R.ok(result);
    }
}
