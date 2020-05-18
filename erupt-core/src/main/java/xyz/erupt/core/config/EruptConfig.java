package xyz.erupt.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liyuepeng
 * @date 2019-10-31.
 */
@Data
@Component
@ConfigurationProperties(prefix = "erupt")
public class EruptConfig {

    //注解扫描根路径
    private String[] scannerPackage = {"xyz.erupt"};

    private String[] allowRequestFileType = {};

    //类路径，需要实现xyz.erupt.annotation.fun.AttachmentProxy接口
    private String attachmentProxy;

    //文件上传根路径
    private String uploadPath = "/opt/file";

    //是否使用redis管理session
    private boolean redisSession = false;

    private String[] dataSources = {};
}
