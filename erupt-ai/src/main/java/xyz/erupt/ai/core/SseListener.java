package xyz.erupt.ai.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.pojo.ChatUsage;

/**
 * @author YuePeng
 * date 2025/2/23 14:34
 */
@Getter
@Setter
public class SseListener {

    // 完整输出内容
    private final StringBuilder output = new StringBuilder();

    private boolean pending = false;

    // 流式输出，当前消息的内容
    private String currMessage;

    // 流式输出，整条消息对象
    private String currData;

    // 花费 token
    private ChatUsage usage = new ChatUsage();

    private boolean isFinish = false;

    private boolean error = false;
}
