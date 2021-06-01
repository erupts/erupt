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

    /**
     * CPU相关信息
     */
    private Cpu cpu;

    /**
     * 內存相关信息
     */
    private Mem mem;

    /**
     * JVM相关信息
     */
    private Jvm jvm;

    /**
     * 服务器相关信息
     */
    private Sys sys;

    /**
     * 磁盘相关信息
     */
    private List<SysFile> sysFiles;

    private Date startupDate; //启动时间

    private String runDay; //运行时长

    public Server() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        this.setCpu(new Cpu(si));
        this.setMem(new Mem(si));
        this.setJvm(new Jvm());
        this.setSys(new Sys());
        long startupDate = EruptSpringUtil.getApplicationContext().getStartupDate();
        this.setStartupDate(new Date(startupDate));
        this.setRunDay(this.zhDateDiff(this.getStartupDate()));
        sysFiles = os.getFileSystem().getFileStores().stream().map(SysFile::new).collect(Collectors.toList());
    }

    private String zhDateDiff(Date date) {
        long l = System.currentTimeMillis() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        return day + "天" + hour + "小时" + min + "分";
    }

}
