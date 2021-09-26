package xyz.erupt.core.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt")
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

    //指定哪些包内的类通过gson实现序列化
    private String[] gsonHttpMessageConvertersPackages;

    private EruptPropDb[] dbs;

}
