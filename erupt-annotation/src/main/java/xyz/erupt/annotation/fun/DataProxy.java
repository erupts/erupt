package xyz.erupt.annotation.fun;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collection;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public interface DataProxy<MODEL> {

    /**
     * 新增前
     *
     * @param model 待新增对象数据
     */
    default void beforeAdd(MODEL model) {
    }

    /**
     * 新增后
     *
     * @param model 已新增对象数据
     */
    default void afterAdd(MODEL model) {
    }

    /**
     * 修改前
     *
     * @param model 待修改对象数据
     */
    default void beforeUpdate(MODEL model) {
    }

    /**
     * 修改后
     *
     * @param model 已修改对象数据
     */
    default void afterUpdate(MODEL model) {
    }

    /**
     * 删除前
     *
     * @param model 待删除对象数据
     */
    default void beforeDelete(MODEL model) {
    }

    /**
     * 删除后
     *
     * @param model 已删除对象数据
     */
    default void afterDelete(MODEL model) {
    }

    /**
     * 查询前动态注入条件
     *
     * @return 自定义查询条件
     */
    default String beforeFetch() {
        return null;
    }

    /**
     * 查询结果处理
     *
     * @param list 查询结果
     */
    default void afterFetch(Collection<Map<String, Object>> list) {
    }


    /**
     * 数据新增行为，对数据做初始化操作
     *
     * @param model 要编辑的数据
     */
    default void addBehavior(MODEL model) {
    }

    /**
     * 数据编辑行为，对待编辑的数据做预处理
     *
     * @param model 要编辑的数据
     */
    default void editBehavior(MODEL model) {
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
