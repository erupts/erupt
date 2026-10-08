package xyz.erupt.ai_tune.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class SamplePageVo {

    private Long total;

    private Integer page;

    private Integer size;

    private List<SampleVo> items;

}
