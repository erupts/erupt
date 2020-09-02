package xyz.erupt.core.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;

import java.util.Arrays;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2019-10-31.
 */
@Data
@Component
@ConfigurationProperties(prefix = "erupt")
public class EruptProp {

    //注解扫描根路径
    private String[] scannerPackage;

    //类路径，需要实现xyz.erupt.annotation.fun.AttachmentProxy接口，可实现自定义附件存储策略
    private String attachmentProxy;

    //文件上传根路径
    private String uploadPath = "/opt/erupt-attachment";

    //是否使用redis管理session
    private boolean redisSession = false;

    private boolean csrfInspect = true;

    private List<DB> dbs;

    public void setScannerPackage(String[] scannerPackage) {
        this.scannerPackage = Arrays.copyOf(scannerPackage, scannerPackage.length + 1);
        this.scannerPackage[scannerPackage.length] = EruptConst.BASE_PACKAGE;
    }


    @Getter
    @Setter
    public static class DB {

        private DataSourceProperties datasource;

        private JpaProperties jpa;

    }
}
