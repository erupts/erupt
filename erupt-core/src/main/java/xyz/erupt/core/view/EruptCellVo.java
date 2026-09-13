package xyz.erupt.core.view;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

/**
 * One cell edit: the row primary key, the field to change and its new value.
 *
 * @author YuePeng
 * date 2026-09-10
 */
@Getter
@Setter
public class EruptCellVo {

    private String id;

    private String field;

    private JsonElement value;

}
