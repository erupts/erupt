package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.monitor.util.SystemUtil;

/**
 * Real-time network / disk IO rate (bytes per second).
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Getter
@Setter
public class Io {

    private long netRecv; // network download rate, bytes/s

    private long netSent; // network upload rate, bytes/s

    private long diskRead; // disk read rate, bytes/s

    private long diskWrite; // disk write rate, bytes/s

    private String netRecvText;

    private String netSentText;

    private String diskReadText;

    private String diskWriteText;

    public void rate(long netRecv, long netSent, long diskRead, long diskWrite) {
        this.netRecv = netRecv;
        this.netSent = netSent;
        this.diskRead = diskRead;
        this.diskWrite = diskWrite;
        this.netRecvText = SystemUtil.formatByte(netRecv) + "/s";
        this.netSentText = SystemUtil.formatByte(netSent) + "/s";
        this.diskReadText = SystemUtil.formatByte(diskRead) + "/s";
        this.diskWriteText = SystemUtil.formatByte(diskWrite) + "/s";
    }

}
