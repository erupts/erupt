package xyz.erupt.webscoket.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YuePeng
 * date 2024/12/8 21:50
 */
@Getter
@AllArgsConstructor
public enum SocketCommand {

    JS("js");

    private final String command;

    @Override
    public String toString() {
        return this.command;
    }
}
