package xyz.erupt.ai_dev.constant;

/**
 * What one line of an agent session's log is.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public enum EventKind {

    // written by erupt itself: workspace preparation, start, stop
    SYSTEM,
    // the agent speaking
    TEXT,
    // the agent calling a tool; name is the tool, content its input
    TOOL,
    TOOL_RESULT,
    // the agent's final answer
    RESULT,
    ERROR

}
