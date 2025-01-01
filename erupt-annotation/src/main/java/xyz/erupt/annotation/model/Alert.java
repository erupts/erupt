package xyz.erupt.annotation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/1/1 21:04
 */
@Getter
@Setter
public class Alert {

    private String message;

    private boolean closeable = false;

    private MessageType messageType = MessageType.INFO;

    public static Alert info(String message) {
        return new Alert() {{
            setMessage(message);
        }};
    }
}
