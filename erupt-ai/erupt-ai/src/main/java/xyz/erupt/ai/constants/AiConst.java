package xyz.erupt.ai.constants;

/**
 * @author YuePeng
 * date 2026/4/8 22:17
 */
public class AiConst {

    public static final String AI_CHAT = "/ai/chat";

    // MetaContext var key holding the chat session ID of the ongoing conversation
    public static final String VAR_CHAT_ID = "ai_chat_id";

    // Session key prefix of the stop signal for an ongoing chat generation: {prefix}{chatId}
    public static final String CHAT_STOP_KEY = "erupt-ai:chat-stop:";

}
