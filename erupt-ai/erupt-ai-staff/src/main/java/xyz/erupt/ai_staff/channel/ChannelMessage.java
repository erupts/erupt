package xyz.erupt.ai_staff.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A user message received from a channel, normalized across platforms.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Getter
@Setter
@Builder
public class ChannelMessage {

    private String content;

    // Platform user ID of the sender
    private String sender;

    private String senderName;

    // Platform-specific reply target: session webhook / channel ID / chat ID / user ID
    private String replyTo;

}
