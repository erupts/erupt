package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.monitor.util.SystemUtil;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2021/1/23 23:27
 */
@Getter
@Setter
public class Jvm {

    private String name;

    private String vendor;

    private String total;

    private String max;

    private String used;

    private String free;

    private String version;

    private String usage;

    private String inputArgs;

    private String pid;

    private String home;

    private String path;

    private int threadCount;

    Jvm() {
        this.setThreadCount(ManagementFactory.getThreadMXBean().getThreadCount());
        Properties props = System.getProperties();
        long total = Runtime.getRuntime().maxMemory();
        long free = Runtime.getRuntime().freeMemory();
        this.setMax(SystemUtil.formatByte(Runtime.getRuntime().maxMemory()));
        this.setTotal(SystemUtil.formatByte(total));
        this.setFree(SystemUtil.formatByte(free));
        this.setUsed(SystemUtil.formatByte(total - free));
        this.setUsage(new DecimalFormat("#.##%").format((total - free) * 1.0 / total));
        this.setVersion(props.getProperty("java.version"));
        this.setHome(props.getProperty("java.home"));
        this.setInputArgs(ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
        this.setPid(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        this.setName(props.getProperty("java.vm.name"));
        this.setVendor(props.getProperty("java.vendor"));
        this.setPath(props.getProperty("user.dir"));
//        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
//        System.out.println("堆内存信息: " + memoryMXBean);
//        System.out.println("方法区内存信息: " + memoryMXBean);
    }

}
