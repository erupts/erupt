package xyz.erupt.cloud.server.distribute;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2022/3/8 21:25
 */
@Getter
@Setter
public class ChannelSwapModel {

    private String instanceId;

    private Command command;

    private Object data;

    private ChannelSwapModel() {
    }

    public static ChannelSwapModel create(String instanceId, Command command, Object data) {
        ChannelSwapModel channelSwapModel = new ChannelSwapModel();
        channelSwapModel.instanceId = instanceId;
        channelSwapModel.command = command;
        channelSwapModel.data = data;
        return channelSwapModel;
    }

    public enum Command {
        PUT, REMOVE
    }

}
