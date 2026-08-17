package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.hardware.CentralProcessor;
import xyz.erupt.monitor.util.SystemUtil;

import java.text.DecimalFormat;

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

    public Cpu(CentralProcessor processor, long[] prevTicks, long[] ticks) {
        long nice = delta(ticks, prevTicks, CentralProcessor.TickType.NICE);
        long irq = delta(ticks, prevTicks, CentralProcessor.TickType.IRQ);
        long softirq = delta(ticks, prevTicks, CentralProcessor.TickType.SOFTIRQ);
        long steal = delta(ticks, prevTicks, CentralProcessor.TickType.STEAL);
        long cSys = delta(ticks, prevTicks, CentralProcessor.TickType.SYSTEM);
        long user = delta(ticks, prevTicks, CentralProcessor.TickType.USER);
        long iowait = delta(ticks, prevTicks, CentralProcessor.TickType.IOWAIT);
        long idle = delta(ticks, prevTicks, CentralProcessor.TickType.IDLE);
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        String freq = SystemUtil.formatByte(processor.getMaxFreq());
        this.setFreq(freq.substring(0, freq.length() - 1) + "Hz");
        this.setCpuNum(processor.getLogicalProcessorCount());
        DecimalFormat fmt = new DecimalFormat("#.##%");
        this.setUser(fmt.format(ratio(user, totalCpu)));
        this.setSys(fmt.format(ratio(cSys, totalCpu)));
        this.setWait(fmt.format(ratio(iowait, totalCpu)));
        this.setUsage(fmt.format(totalCpu <= 0 ? 0 : 1.0 - (idle * 1.0 / totalCpu)));
        this.setCpuModel(processor.toString());
    }

    private static long delta(long[] ticks, long[] prevTicks, CentralProcessor.TickType type) {
        return ticks[type.getIndex()] - prevTicks[type.getIndex()];
    }

    private static double ratio(long value, long total) {
        return total <= 0 ? 0 : value * 1.0 / total;
    }

}
