package xyz.erupt.core.operator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/9/4 23:12
 */
@Getter
@Setter
public class EruptSearchModel {

    private String field;

    private OperatorType operatorType;

    private String operator;

    private Object value;

}
