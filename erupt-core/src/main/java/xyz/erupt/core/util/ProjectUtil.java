package xyz.erupt.core.util;

import lombok.extern.java.Log;

import java.io.File;
import java.util.function.Consumer;

/**
 * Created by liyuepeng on 2019-05-21.
 */
@Log
public class ProjectUtil {

    /**
     * @param projectName 标识名
     * @param first       bool回调用来告之函数调用者是否为第一次调用
     */
    public void projectStartLoaded(String projectName, Consumer<Boolean> first) {
        //创建 loaded文件夹
        File dirFile = new File(this.getClass().getClassLoader().getResource("").getPath() + "loaded");
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                log.severe("项目加载标识文件无法创建，可能造成数据多次加载等问题");
            }
        }
        File file = new File(dirFile.getPath(), projectName + ".loaded");
        try {
            if (file.exists()) {
                first.accept(false);
            } else {
                first.accept(true);
                if (!file.createNewFile()) {
                    log.severe("项目加载标识文件无法创建，可能造成数据多次加载等问题");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("代码执行出错，位置：" + file.getPath() + "标识文件无法创建");
        }
    }
}
