package xyz.erupt.server.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

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

    public Server() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        this.setCpu(new Cpu(si));
        this.setJvm(new Jvm());
        this.setMem(new Mem(si));
        this.setSys(new Sys());
        for (OSFileStore store : os.getFileSystem().getFileStores()) {
            sysFiles.add(new SysFile(store));
        }
    }

}
