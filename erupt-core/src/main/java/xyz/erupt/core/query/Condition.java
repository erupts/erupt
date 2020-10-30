package xyz.erupt.core.query;

import lombok.Data;
import xyz.erupt.annotation.sub_field.EditType;

@Data
public class Condition {

    private String key;

    private Object value;

    private Type valueType;

    private EditType editType;

    enum Type {
        INPUT, RANGE, IN
    }
}
