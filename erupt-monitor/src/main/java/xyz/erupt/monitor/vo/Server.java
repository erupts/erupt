package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2021/1/23 23:21
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
    private List<SysFile> sysFiles = new LinkedList<>();

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
        for (OSFileStore store : os.getFileSystem().getFileStores()) {
            sysFiles.add(new SysFile(store));
        }
    }

    private String zhDateDiff(Date date) {
        long l = System.currentTimeMillis() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        return day + "天" + hour + "小时" + min + "分";
    }

}
