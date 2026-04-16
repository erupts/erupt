package xyz.erupt.ai.cube;

import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Measure;

import java.util.Date;

/**
 * @author YuePeng
 * date 2026/4/10 23:29
 */
@EruptCube(
        name = "Erupt AI Chat",
        sql = """
                select user.name,
                       message.sender_type as senderType,
                       message.llm,
                       message.model,
                       message.tokens,
                       message.created_at  as createdAt,
                       agent.name          as agent
                from e_ai_chat_message message
                         inner join e_ai_chat chat on chat.id = message.chat_id
                         left join e_upms_user user on chat.user_id = user.id
                         left join e_ai_llm_agent agent on agent.id = message.agent_id
                """
)
public class EruptAIChatCube {

    @Dimension(title = "User")
    private String name;

    @Dimension(title = "Sender Type")
    private String senderType;

    @Dimension(title = "LLM")
    private String llm;

    @Dimension(title = "Model")
    private String model;

    @Dimension(title = "Agent")
    private String agent;

    @Dimension(title = "Date")
    private Date createdAt;

    @Measure(title = "Message Count", sql = "count(*)")
    private Long messageCount;

    @Measure(title = "Total Tokens", sql = "sum(tokens)")
    private Long totalTokens;

    @Measure(title = "Avg Tokens", sql = "avg(tokens)")
    private Double avgTokens;

}
