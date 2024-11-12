package xyz.erupt.core.util;

import lombok.AllArgsConstructor;
import xyz.erupt.annotation.fun.PowerObject;

import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2024/11/12 22:28
 */
public class EruptTableStyle {

    public static void rowPower(Map<String, Object> row, PowerObject powerObject) {
        row.put(RowStyle.POWER.key, powerObject);
    }

    public static void rowPower(Map<String, Object> row, boolean viewDetails, boolean edit, boolean delete) {
        PowerObject powerObject = new PowerObject();
        powerObject.setViewDetails(viewDetails);
        powerObject.setEdit(edit);
        powerObject.setDelete(delete);
        row.put(RowStyle.POWER.key, powerObject);
    }

    public static void cellColor(Map<String, Object> row, String field, String color) {
        Optional.ofNullable(row.get(field)).ifPresent(it -> row.put(field, String.format("<span style='color:%s'>%s<span>", color, it)));
    }


    @AllArgsConstructor
    public enum RowStyle {
        POWER("__power__", PowerObject.class),

        ;

        private final String key;

        private final Class<?> type;
    }

}
