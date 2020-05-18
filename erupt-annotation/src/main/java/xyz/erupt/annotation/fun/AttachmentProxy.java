package xyz.erupt.annotation.fun;

import java.io.InputStream;

/**
 * @author liyuepeng
 * @date 2020-05-18
 */
public interface AttachmentProxy {

    void upLoad(InputStream inputStream, String path);

    default boolean isLocalSave() {
        return true;
    }

    String fileDomain();
}
