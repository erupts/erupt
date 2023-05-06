package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.model.Row;
import xyz.erupt.annotation.query.Condition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface DataProxy<@Comment("Erupt类对象") MODEL> extends MetaProxy<MODEL> {

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
    default String beforeFetch(List<Condition> conditions) {
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

    //必须用参数的形式传递model, 因为dataProxy可出现多层定义，返回值方式无法多层传递对象
    @Comment("默认查询条件")
    default void searchCondition(Map<String, Object> condition) {

    }

    @Comment("excel导出，参数需要强转为WorkBook对象")
    default void excelExport(@Comment("POI文档对象") Object workbook) {
    }

    @Comment("excel导入，参数需要强转为WorkBook对象")
    default void excelImport(@Comment("POI文档对象") Object workbook) {
    }

    @Comment("自定义行，可实现行计算等能力")
    default List<Row> extraRow(List<Condition> conditions) {
        return null;
    }

}
