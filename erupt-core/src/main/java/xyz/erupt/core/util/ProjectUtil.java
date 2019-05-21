package xyz.erupt.core.util;

import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by liyuepeng on 2019-05-21.
 */
@Log
public class ProjectUtil {

    /**
     * @param projectName 标识名
     * @param consumer    bool回调用来告之调用者函数是否为第一次调用
     */
    public void ProjectStartLoaded(String projectName, Consumer<Boolean> consumer) {
        //创建 loaded文件夹
        File dirFile = new File(this.getClass().getClassLoader().getResource("").getPath() + "loaded");
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                log.warning("项目加载标识文件无法创建，可能造成数据多次加载等问题");
            }
        }
        File file = new File(dirFile.getPath(), projectName + ".loaded");
        if (!file.exists()) {
            consumer.accept(true);
            try {
                if (!file.createNewFile()) {
                    log.warning("项目加载标识文件无法创建，可能造成数据多次加载等问题");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            consumer.accept(false);
        }
    }
}
