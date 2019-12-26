package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface DataProxy<MODEL> {

    //增
    default void beforeAdd(MODEL model) {

    }

    default void afterAdd(MODEL model) {
    }

    //改
    default void beforeUpdate(MODEL model) {
    }

    default void afterUpdate(MODEL model) {
    }

    //删
    default void beforeDelete(Serializable id) {

    }

    default void afterDelete(Serializable id) {
    }

    /**
     * 查询
     * @param condition 用户输入条件
     * @return 自定义查询条件
     */
    default String beforeFetch(JsonObject condition) {
        return null;
    }

    default void afterFetch(Collection<Map<String, Object>> list) {
    }

    //编辑数据行为
    default void editBehavior(MODEL model) {

    }

    //文件上传
    default void beforeUpLoadFile(InputStream inputStream, File file) {

    }

    default void afterUpLoadFile(File file, String relativePath) {
    }

    /**
     * excel导出
     *
     * @param wb POI文档对象
     */
    default void excelExport(Workbook wb) {
    }

    //excel导入
    default void excelImport(MODEL model) {

    }

}
