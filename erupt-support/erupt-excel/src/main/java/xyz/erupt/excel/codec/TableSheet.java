package xyz.erupt.excel.codec;

import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.List;

/**
 * A format-neutral table ready to be written: titled columns and rows of display values. Cell
 * values are String, Number, Boolean, LocalDate or LocalDateTime (or null); text formats render the
 * temporal ones as ISO-8601, Excel keeps them as dates.
 *
 * @param conditions the query that produced the rows, as (title, operator, value) triples; a codec may list them
 * @param template   header only, columns taken from the form rather than the table
 */
public record TableSheet(EruptModel eruptModel, String title, List<TableColumn> columns, List<List<Object>> rows,
                         List<String[]> conditions, boolean template) {

    /**
     * A column names its field and the view it came from (-1 for a form column). Annotations are
     * resolved through the field on every call: a proxied {@code Edit} or {@code View} infers AUTO
     * from the thread's current field, so one captured earlier would answer for another column.
     */
    public record TableColumn(String title, EruptFieldModel field, int viewIndex) {

        public Edit edit() {
            return field.getEruptField().edit();
        }

        public View view() {
            return viewIndex < 0 ? null : field.getEruptField().views()[viewIndex];
        }

    }

}
