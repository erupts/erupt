package xyz.erupt.annotation.fun;

import org.apache.poi.ss.usermodel.Workbook;
import xyz.erupt.annotation.config.Comment;

import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface DataProxy<@Comment("Erupt类对象") MODEL> {

    @Comment("增加前")
    default void beforeAdd(MODEL model) {
    }

    @Comment("增加后")
    default void afterAdd(MODEL model) {
    }

    @Comment("修改前")
    default void beforeUpdate(MODEL model) {
    }

    @Comment("修改后")
    default void afterUpdate(MODEL model) {
    }

    @Comment("删除前")
    default void beforeDelete(MODEL model) {
    }

    @Comment("删除后")
    default void afterDelete(MODEL model) {
    }

    @Comment("查询前，返回值为：自定义查询条件")
    default String beforeFetch(Class<?> eruptClass) {
        return null;
    }

    @Comment("查询后结果处理")
    default void afterFetch(@Comment("查询结果") Collection<Map<String, Object>> list) {
    }


    @Comment("数据新增行为，可对数据做初始化等操作")
    default void addBehavior(MODEL model) {
    }

    @Comment("数据编辑行为，对待编辑的数据做预处理")
    default void editBehavior(MODEL model) {
    }

    @Comment("excel导出")
    default void excelExport(@Comment("POI文档对象") Workbook wb) {
    }

    @Comment("excel导入")
    @Deprecated
    default void excelImport(MODEL model) {
    }

}
