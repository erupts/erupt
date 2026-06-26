package xyz.erupt.monitor.service;

import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import xyz.erupt.monitor.vo.Io;

/**
 * Stateful sampler that derives IO rates from the delta between two consecutive
 * polls, so it naturally rides on the page's existing refresh interval without
 * introducing an extra blocking sleep.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Service
public class IoSampleService {

    private final SystemInfo systemInfo = new SystemInfo();

    private long prevTime;

    private long prevNetRecv, prevNetSent, prevDiskRead, prevDiskWrite;

    private boolean inited;

    public synchronized Io sample() {
        long now = System.currentTimeMillis();
        long netRecv = 0, netSent = 0;
        for (NetworkIF nif : systemInfo.getHardware().getNetworkIFs()) {
            nif.updateAttributes();
            netRecv += nif.getBytesRecv();
            netSent += nif.getBytesSent();
        }
        long diskRead = 0, diskWrite = 0;
        for (HWDiskStore disk : systemInfo.getHardware().getDiskStores()) {
            disk.updateAttributes();
            diskRead += disk.getReadBytes();
            diskWrite += disk.getWriteBytes();
        }
        Io io = new Io();
        if (inited) {
            double sec = Math.max((now - prevTime) / 1000.0, 1);
            io.rate(perSec(netRecv - prevNetRecv, sec), perSec(netSent - prevNetSent, sec),
                    perSec(diskRead - prevDiskRead, sec), perSec(diskWrite - prevDiskWrite, sec));
        } else {
            io.rate(0, 0, 0, 0);
            inited = true;
        }
        prevTime = now;
        prevNetRecv = netRecv;
        prevNetSent = netSent;
        prevDiskRead = diskRead;
        prevDiskWrite = diskWrite;
        return io;
    }

    // guard counter reset / overflow
    private long perSec(long delta, double sec) {
        return delta <= 0 ? 0 : (long) (delta / sec);
    }

}
