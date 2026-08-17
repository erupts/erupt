package xyz.erupt.webscoket.channel;

/**
 * @author YuePeng
 * date 2024/12/8 21:50
 */
public enum SocketCommand {

    JS("js");

    private final String command;

    SocketCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return this.command;
    }
}
