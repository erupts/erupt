package xyz.erupt.monitor.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.JvmDiagnosis;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Application-level diagnosis: deep JVM internals and an on-demand thread dump.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/diagnosis")
public class MonitorDiagnosisController {

    @GetMapping("/jvm")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public JvmDiagnosis jvm() {
        return new JvmDiagnosis();
    }

    @GetMapping("/thread-dump")
    @EruptMenuAuth(MonitorConstant.MENU_DIAGNOSIS)
    public void threadDump(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=thread-dump.txt");
        response.getOutputStream().write(JvmDiagnosis.dumpThreads().getBytes(StandardCharsets.UTF_8));
    }

}
