package xyz.erupt.annotation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/1/1 21:04
 */
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    /** Text to show */
    private String message;

    /** Whether the user may dismiss it */
    private boolean closeable = false;

    /** success, info, warning or error */
    private UiType uiType = UiType.info;

    public static Alert info(String message) {
        return new Alert(message, false, UiType.info);
    }

    public Alert(String message, boolean closeable, UiType uiType) {
        this.message = message;
        this.closeable = closeable;
        this.uiType = uiType;
    }
}
