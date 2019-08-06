package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Workbook;
import xyz.erupt.annotation.model.BoolAndReason;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface DataProxy<MODEL> {

    //改
    default BoolAndReason beforeEdit(MODEL model) {
        return new BoolAndReason(true, null);
    }

    default void afterEdit(MODEL model) {
    }

    //增
    default BoolAndReason beforeAdd(MODEL model) {
        return new BoolAndReason(true, null);
    }

    default void afterAdd(MODEL model) {
    }

    //删
    default BoolAndReason beforeDelete(Serializable id) {
        return new BoolAndReason(true, null);
    }

    default void afterDelete(Serializable id) {
    }

    /**
     * 查询
     *
     * @param condition 用户输入条件
     * @return 自定义查询条件
     */
    default String beforeFetch(JsonObject condition) {
        return null;
    }

    default void afterFetch(Collection<Map<String, Object>> list) {
    }

    //文件上传
    default BoolAndReason beforeUpLoadFile(InputStream inputStream, File file) {
        return new BoolAndReason(true, null);
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

    /**
     * excel导入
     *
     * @param model erupt对象模型
     */
    default BoolAndReason excelImport(MODEL model) {
        return new BoolAndReason(true, null);
    }

}
