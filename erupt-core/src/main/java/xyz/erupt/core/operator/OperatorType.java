package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YuePeng
 * date 2025/9/4 23:13
 */
@AllArgsConstructor
@Getter
public enum OperatorType {
    STRING(OperatorStringType.class),
    NUMBER(OperatorNumberType.class),
    DATE(OperatorDateType.class),
    CHOICE(OperatorReferenceType.class),
    BOOLEAN(OperatorReferenceType.class),
    REFERENCE(OperatorReferenceType.class),

    UPMS(OperatorUpmsType.class)
    ;

    public final Class<? extends Enum<?>> operatorClass;

}
