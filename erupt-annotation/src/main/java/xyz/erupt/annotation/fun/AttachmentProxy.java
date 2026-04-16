package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.io.InputStream;

/**
 * @author YuePeng
 * date 2020-05-18
 */
@Comment("Custom attachment upload strategy")
public interface AttachmentProxy {

    @Comment("Attachment upload")
    @Comment("The return value represents the storage path; in most cases returning path is sufficient")
    String upLoad(@Comment("data stream") InputStream inputStream, @Comment("upload path") String path);

    @Comment("Domain name where attachments are hosted")
    String fileDomain();

    @Comment("Whether to also save to the local server")
    default boolean isLocalSave() {
        return true;
    }
}
