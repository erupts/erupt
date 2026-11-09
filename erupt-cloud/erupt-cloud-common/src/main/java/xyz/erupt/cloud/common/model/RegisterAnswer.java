package xyz.erupt.cloud.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The server's answer to a register / heartbeat call (PROTOCOL.md §12.2): the newest protocol version it speaks.
 * A server older than version 2 answers no body, which a v2 node reads as "too old".
 *
 * @author YuePeng
 * date 2026/11/05
 */
@Getter
@Setter
public class RegisterAnswer {

    private Integer protocolVersion;

    public RegisterAnswer() {
    }

    public RegisterAnswer(Integer protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

}
