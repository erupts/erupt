package xyz.erupt.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt")
public class EruptProp {

    //热构建erupt,开启此功能后每次请求都会重新构建erupt，该功能方便启动时修改erupt代码
    @Deprecated
    private boolean hotBuild = false;

    //附件存储根路径
    private String uploadPath = "/opt/erupt-attachment";

    //是否使用redis管理session
    private boolean redisSession = false;

    //是否开启csrf校验
    private boolean csrfInspect = true;

    //是否保留上传文件原始名称
    private boolean keepUploadFileName = false;

    @Deprecated
    private String[] jacksonHttpMessageConvertersPackages;

    private String[] gsonHttpMessageConvertersPackages;

    private List<EruptPropDb> dbs;


}
