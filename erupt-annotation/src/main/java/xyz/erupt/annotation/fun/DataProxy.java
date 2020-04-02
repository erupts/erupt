package xyz.erupt.annotation.fun;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public interface DataProxy<MODEL> {

    /**
     * 增
     *
     * @param model 待新增对象数据
     */
    default void beforeAdd(MODEL model) {

    }

    /**
     * 增
     *
     * @param model 已新增对象数据
     */
    default void afterAdd(MODEL model) {
    }

    /**
     * 改
     *
     * @param model 待修改对象数据
     */
    default void beforeUpdate(MODEL model) {
    }

    /**
     * 改
     *
     * @param model 已修改对象数据
     */
    default void afterUpdate(MODEL model) {
    }

    /**
     * 删
     *
     * @param model 待删除对象数据
     */
    default void beforeDelete(MODEL model) {

    }

    /**
     * 删
     *
     * @param model 已删除对象数据
     */
    default void afterDelete(MODEL model) {
    }

    /**
     * 查询
     *
     * @return 自定义查询条件
     */
    default String beforeFetch() {
        return null;
    }

    /**
     * 查询
     *
     * @param list 查询结果
     */
    default void afterFetch(Collection<Map<String, Object>> list) {
    }

    /**
     * 编辑数据行为操作
     *
     * @param model 要编辑的数据
     */
    default void editBehavior(MODEL model) {

    }

    /**
     * 待上传文件
     *
     * @param inputStream 数据流
     * @param file        文件对象
     */
    default void beforeUpLoadFile(InputStream inputStream, File file) {

    }

    /**
     * 已上传文件
     *
     * @param relativePath 文件上传路径
     * @param file         文件对象
     */
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
     * @param model 待导入对象数据
     */
    default void excelImport(MODEL model) {

    }

}
