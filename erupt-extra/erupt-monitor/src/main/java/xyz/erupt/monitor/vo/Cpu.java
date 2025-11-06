package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import xyz.erupt.monitor.util.SystemUtil;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2021/1/23 23:04
 */
@Getter
@Setter
public class Cpu {

    private int cpuNum;

    private String freq;

    private String sys;

    private String user;

    private String wait;

    private String usage;

    private String cpuModel;

    @SneakyThrows
    Cpu(SystemInfo systemInfo, Boolean wait) {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        if (null == wait || wait) {
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        String freq = SystemUtil.formatByte(processor.getMaxFreq());
        this.setFreq(freq.substring(0, freq.length() - 1) + "Hz");
        this.setCpuNum(processor.getLogicalProcessorCount());
        this.setUser(new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
        this.setSys(new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
        this.setWait(new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));
        this.setUsage(new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
        this.setCpuModel(processor.toString());
    }

}
