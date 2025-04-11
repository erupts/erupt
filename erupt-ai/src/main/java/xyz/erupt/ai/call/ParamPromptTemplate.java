package xyz.erupt.ai.call;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/4/9 22:31
 */
@Getter
@Setter
public class ParamPromptTemplate {

    private boolean required;

    private String type;

    private String description;

    private Object val;

}
