package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.software.os.OSFileStore;
import xyz.erupt.monitor.util.SystemUtil;

import java.text.DecimalFormat;

/**
 * @author YuePeng
 * date 2021/1/23 23:07
 */
@Getter
@Setter
public class SysFile {

    /**
     * Drive letter path
     */
    private String dirName;

    /**
     * Drive letter type
     */
    private String sysTypeName;

    /**
     * File type
     */
    private String typeName;

    private String total;

    private String free;

    private String used;

    /**
     * Utilization rate
     */
    private String usage;

    SysFile(OSFileStore fs) {
        long free = fs.getUsableSpace();
        long total = fs.getTotalSpace();
        long used = total - free;
        this.setDirName(fs.getMount());
        this.setSysTypeName(fs.getType());
        this.setTypeName(fs.getName());
        this.setTotal(SystemUtil.formatByte(total));
        this.setFree(SystemUtil.formatByte((free)));
        this.setUsed(SystemUtil.formatByte((used)));
        this.setUsage(new DecimalFormat("#.##%").format((total - free) * 1.0 / total));
    }

}
