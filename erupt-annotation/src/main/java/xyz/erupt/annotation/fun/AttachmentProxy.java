package xyz.erupt.annotation.fun;

import java.io.InputStream;

/**
 * @author liyuepeng
 * @date 2020-05-18
 */
public interface AttachmentProxy {

    /**
     * @param inputStream 数据流
     * @param path        上传位置
     */
    void upLoad(InputStream inputStream, String path);

    /**
     * @return 附件域名
     */
    String fileDomain();

    /**
     * @return 是否同时保存到本地服务器
     */
    default boolean isLocalSave() {
        return true;
    }
}
