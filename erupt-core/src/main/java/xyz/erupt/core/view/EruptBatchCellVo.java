package xyz.erupt.core.view;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * One batch edit: the selected rows, the field to change and the value every row receives.
 *
 * @author YuePeng
 * date 2026-10-20
 */
@Getter
@Setter
public class EruptBatchCellVo {

    /** Primary keys of the rows */
    private List<String> ids;

    /** Field name */
    private String field;

    /** New value, applied to every row */
    private JsonElement value;

}
