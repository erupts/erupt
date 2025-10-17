package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import xyz.erupt.monitor.util.SystemUtil;

import java.text.DecimalFormat;

/**
 * @author YuePeng
 * date 2021/1/23 23:05
 */
@Getter
@Setter
public class Mem {

    private String total;

    private String used;

    private String free;

    private String usage;

    Mem(SystemInfo systemInfo) {
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();
        this.setTotal(SystemUtil.formatByte(totalByte));
        this.setUsed(SystemUtil.formatByte(totalByte - availableByte));
        this.setFree(SystemUtil.formatByte(availableByte));
        this.setUsage(new DecimalFormat("#.##%").format((totalByte - availableByte) * 1.0 / totalByte));
    }

}
