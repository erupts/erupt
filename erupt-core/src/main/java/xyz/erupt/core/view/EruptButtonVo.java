package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Getter
@Setter
public class EruptButtonVo {

    private String eval;

    private Map<String, Object> formData;

    private Map<String, String> editExpr;

}
