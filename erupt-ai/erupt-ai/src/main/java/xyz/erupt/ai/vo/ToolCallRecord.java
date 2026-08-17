package xyz.erupt.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 */
@Getter
@AllArgsConstructor
public class ToolCallRecord {

    private String name;

    private String args;

    private String result;

    private LocalDateTime time;

}
