package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author YuePeng
 * date 2026/1/2 23:42
 */
@Getter
@Setter
public class OnChangeVo {

    /** Field values to write into the form */
    private Map<String, Object> formData;

    /** Field name to edit expression to apply */
    private Map<String, String> editExpr;

}
