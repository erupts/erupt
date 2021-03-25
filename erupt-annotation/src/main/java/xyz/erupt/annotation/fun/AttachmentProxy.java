package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.io.InputStream;

/**
 * @author YuePeng
 * date 2020-05-18
 */
@Comment("自定义附件上传策略")
public interface AttachmentProxy {

    @Comment("附件上传")
    void upLoad(@Comment("数据流") InputStream inputStream, @Comment("上传位置") String path);

    @Comment("附件所在域名")
    String fileDomain();

    @Comment("是否同时保存到本地服务器")
    default boolean isLocalSave() {
        return true;
    }
}
