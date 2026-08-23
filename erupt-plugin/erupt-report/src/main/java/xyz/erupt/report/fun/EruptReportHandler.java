package xyz.erupt.report.fun;

import org.apache.poi.ss.usermodel.Workbook;
import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
public interface EruptReportHandler {

    /**
     * Dynamic query expression handler
     *
     * @param param handler class parameter
     * @param expr  query expression
     * @return new expression string
     */
    @Comment("Dynamic query expression handler")
    default String exprHandler(@Comment("Handler class parameter") String param,
                               @Comment("Query conditions") Map<String, Object> condition,
                               @Comment("Query expression") String expr) {

        return expr;
    }

    /**
     * Result processor
     *
     * @param param  handler class parameter
     * @param result query result
     */
    @Comment("Result processor")
    default void resultHandler(@Comment("Handler class parameter") String param,
                               @Comment("Query conditions") Map<String, Object> condition,
                               @Comment("Query result") List<Map<String, Object>> result) {

    }

    /**
     * Excel export handler
     *
     * @param condition query conditions
     * @param workbook  initialized POI workbook
     */
    @Comment("Excel export handler")
    default void exportHandler(@Comment("Handler class parameter") String param,
                               @Comment("Query conditions") Map<String, Object> condition,
                               @Comment("Initialized POI workbook") Workbook workbook) {

    }
}
