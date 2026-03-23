package xyz.erupt.ai.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/23 23:30
 */
@Getter
@Setter
public class AgentVo {

    private Long id;

    private String name;

    private String desc;

    private List<String> hints;

}
