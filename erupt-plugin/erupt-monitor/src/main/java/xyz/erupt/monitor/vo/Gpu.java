package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import oshi.hardware.GraphicsCard;

/**
 * @author YuePeng
 * date 2025/1/3 23:38
 */
@Getter
@Setter
public class Gpu {

    private String name;

    private String deviceId;

    private String vendor;

    private String versionInfo;

    private long memorySize;

    // Constructor
    Gpu(GraphicsCard graphicsCard) {
        this.setName(graphicsCard.getName());
        this.setDeviceId(graphicsCard.getDeviceId());
        this.setVendor(graphicsCard.getVendor());
        this.setVersionInfo(graphicsCard.getVersionInfo());
        this.setMemorySize(graphicsCard.getVRam() / 1073741824);
    }
}
