package xyz.erupt.bi.semantic.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

/**
 * @author YuePeng
 * date 2025/11/2 21:20
 */
@Getter
@Setter
public class Dashboard extends HyperModelUpdateVo {

    private String code;

    private String name;

    private String description;

    private String cube;

    private String dsl;

    private String draftDsl;

}
