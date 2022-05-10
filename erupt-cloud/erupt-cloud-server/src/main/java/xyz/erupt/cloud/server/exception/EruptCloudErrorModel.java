package xyz.erupt.cloud.server.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2022/2/24 00:58
 */
@Getter
@Setter
public class EruptCloudErrorModel {

    int status;

    String message;

    String node;

    public EruptCloudErrorModel(int status, String message, String node) {
        this.status = status;
        this.message = message;
        this.node = node;
    }
}
