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
     * 盘符路径
     */
    private String dirName;

    /**
     * 盘符类型
     */
    private String sysTypeName;

    /**
     * 文件类型
     */
    private String typeName;

    /**
     * 总大小
     */
    private String total;

    /**
     * 剩余大小
     */
    private String free;

    /**
     * 已经使用量
     */
    private String used;

    /**
     * 资源的使用率
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
