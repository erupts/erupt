package xyz.erupt.bi.fun;

import org.apache.poi.ss.usermodel.Workbook;
import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
public interface EruptBiHandler {

    /**
     * 表达式处理
     *
     * @param param 参数
     * @param expr  表达式
     * @return
     */
    @Comment("查询表达式动态处理")
    default String exprHandler(@Comment("参数") String param, @Comment("查询表达式") String expr) {

        return expr;
    }

    /**
     * 返回结果处理
     *
     * @param param  参数
     * @param result 结果
     */
    @Comment("返回结果处理")
    default void resultHandler(@Comment("参数") String param, @Comment("查询结果") List<Map<String, Object>> result) {

    }

    /**
     * 导出excel处理
     *
     * @param condition 查询条件
     * @param workbook  poi对象
     */
    @Comment("导出excel处理")
    default void exportHandler(@Comment("查询条件") Map<String, Object> condition, @Comment("poi对象") Workbook workbook) {

    }
}
