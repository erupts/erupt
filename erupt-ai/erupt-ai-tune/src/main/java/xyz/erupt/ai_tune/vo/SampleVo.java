package xyz.erupt.ai_tune.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_tune.model.TuneSample;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class SampleVo {

    private Long id;

    private Integer seq;

    private String source;

    private String content;

    private Integer turns;

    private Integer tokens;

    private Boolean valid;

    private String errorInfo;

    public static SampleVo of(TuneSample sample) {
        SampleVo vo = new SampleVo();
        vo.setId(sample.getId());
        vo.setSeq(sample.getSeq());
        vo.setSource(sample.getSource());
        vo.setContent(sample.getContent());
        vo.setTurns(sample.getTurns());
        vo.setTokens(sample.getTokens());
        vo.setValid(sample.getValid());
        vo.setErrorInfo(sample.getErrorInfo());
        return vo;
    }

}
