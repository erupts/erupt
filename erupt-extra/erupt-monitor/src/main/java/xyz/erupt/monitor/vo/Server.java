package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/1/23 23:21
 */
@Getter
@Setter
public class Server {

    private Cpu cpu;

    private Mem mem;

    private Jvm jvm;

    private Sys sys;

    private List<Gpu> gpus;

    /**
     * Disk
     */
    private List<SysFile> sysFiles;

    private Date startupDate; // starting time

    private String runDay; // Running duration

    public Server(Boolean waitCpu) {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        this.setCpu(new Cpu(si, waitCpu));
        this.setMem(new Mem(si));
        this.setJvm(new Jvm());
        this.setSys(new Sys());
        this.setGpus(si.getHardware().getGraphicsCards().stream().map(Gpu::new).collect(Collectors.toList()));
        this.setSysFiles(os.getFileSystem().getFileStores().stream().map(SysFile::new).collect(Collectors.toList()));
        long startupDate = EruptSpringUtil.getApplicationContext().getStartupDate();
        this.setStartupDate(new Date(startupDate));
        this.setRunDay(this.zhDateDiff(this.getStartupDate()));
    }

    private String zhDateDiff(Date date) {
        long l = System.currentTimeMillis() - date.getTime();
        long days = l / (24 * 60 * 60 * 1000);
        long hours = (l / (60 * 60 * 1000) - days * 24);
        long mins = ((l / (60 * 1000)) - days * 24 * 60 - hours * 60);
        return String.format("%s%s%s",
                days > 0 ? days + " days " : "",
                hours > 0 ? hours + " hours " : "",
                mins + " minutes"
        ).trim();
    }

}
