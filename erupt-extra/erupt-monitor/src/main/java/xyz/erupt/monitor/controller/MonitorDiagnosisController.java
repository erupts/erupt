package xyz.erupt.monitor.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.service.HttpStatService;
import xyz.erupt.monitor.vo.HttpStat;
import xyz.erupt.monitor.vo.JvmDiagnosis;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Application-level diagnosis: deep JVM internals and an on-demand thread dump.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/diagnosis")
public class MonitorDiagnosisController {

    @Resource
    private HttpStatService httpStatService;

    @GetMapping("/jvm")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public JvmDiagnosis jvm() {
        return new JvmDiagnosis();
    }

    @GetMapping("/http-stats")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public List<HttpStat> httpStats(@RequestParam(defaultValue = "avg") String sortBy,
                                    @RequestParam(defaultValue = "20") int limit) {
        return httpStatService.top(limit, sortBy);
    }

    @DeleteMapping("/http-stats")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public void resetHttpStats() {
        httpStatService.reset();
    }

    @GetMapping("/thread-dump")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public void threadDump(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=thread-dump.txt");
        response.getOutputStream().write(JvmDiagnosis.dumpThreads().getBytes(StandardCharsets.UTF_8));
    }

}
