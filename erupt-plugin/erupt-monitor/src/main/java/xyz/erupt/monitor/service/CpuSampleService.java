package xyz.erupt.monitor.service;

import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import xyz.erupt.monitor.vo.Cpu;

/**
 * Stateful CPU sampler that derives usage from the tick delta between two
 * consecutive polls, so it rides on the page's existing refresh interval
 * instead of blocking the request thread with a 1s sleep.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Service
public class CpuSampleService {

    private final CentralProcessor processor = new SystemInfo().getHardware().getProcessor();

    private long[] prevTicks;

    public CpuSampleService() {
        // prime the baseline at startup so the first poll already yields a real value
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    public synchronized Cpu sample() {
        long[] prev = prevTicks;
        long[] ticks = processor.getSystemCpuLoadTicks();
        prevTicks = ticks;
        return new Cpu(processor, prev, ticks);
    }

}
