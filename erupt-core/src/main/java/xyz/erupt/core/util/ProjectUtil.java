package xyz.erupt.core.util;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.core.constant.EruptConst;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author liyuepeng
 * @date 2019-05-21.
 */
@Slf4j
public class ProjectUtil {

    private static final String LOADED_EXT = ".loaded";

    /**
     * @param projectName 标识名
     * @param first       bool回调，表示函数是否为第一次调用
     */
    public void projectStartLoaded(String projectName, Consumer<Boolean> first) {
        String userDir = System.getProperty("user.dir");
        File dirFile = new File(userDir, EruptConst.ERUPT_DIR);
        String warnTxt = "项目加载标识文件无法创建，可能造成数据多次加载等问题";
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                log.warn(dirFile.toString() + warnTxt);
            }
        }
        File file = new File(dirFile.getPath(), projectName + LOADED_EXT);
        if (file.exists()) {
            first.accept(false);
        } else {
            try {
                first.accept(true);
                if (!file.createNewFile()) {
                    log.warn(dirFile.toString() + warnTxt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
