package xyz.erupt.server.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.server.util.SystemUtil;

import java.text.DecimalFormat;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2021/1/23 23:27
 */
@Getter
@Setter
public class Jvm {

    private String total;

    private String max;

    private String used;

    private String free;

    private String home;

    private String version;

    private String usage;

    Jvm() {
        Properties props = System.getProperties();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        this.setMax(SystemUtil.formatByte(Runtime.getRuntime().maxMemory()));
        this.setTotal(SystemUtil.formatByte(total));
        this.setFree(SystemUtil.formatByte(free));
        this.setUsed(SystemUtil.formatByte(total - free));
        this.setUsage(new DecimalFormat("#.##%").format((total - free) * 1.0 / total));
        this.setVersion(props.getProperty("java.version"));
        this.setHome(props.getProperty("java.home"));
    }
}
