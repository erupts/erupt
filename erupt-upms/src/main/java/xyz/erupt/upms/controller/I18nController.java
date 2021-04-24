//package xyz.erupt.upms.controller;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import sun.misc.Unsafe;
//import xyz.erupt.core.constant.EruptRestPath;
//import xyz.erupt.upms.service.EruptSessionService;
//import xyz.erupt.upms.util.IpUtil;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author YuePeng
// * date 2021/4/19 17:57
// */
//@RestController
//@RequestMapping(EruptRestPath.ERUPT_API + "/i18n")
//public class I18nController {
//
//    public static final String I18N_KEY = "i18n:";
//
//    @Resource
//    private EruptSessionService eruptSessionService;
//
//    @RequestMapping("/switch")
//    public void switchI18N(@RequestParam String language, HttpServletRequest request) {
//        eruptSessionService.put("" + IpUtil.getIpAddr(request), language, 100);
//    }
//
//}
