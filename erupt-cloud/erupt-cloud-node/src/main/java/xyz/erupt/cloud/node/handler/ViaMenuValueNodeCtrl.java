package xyz.erupt.cloud.node.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.cloud.node.service.ServerRemoteService;

/**
 * @author YuePeng
 * date 2022/3/8 22:19
 */
@Component
public class ViaMenuValueNodeCtrl implements ExprBool.ExprHandler {

    @Resource
    private ServerRemoteService serverRemoteService;

    @Override
    public boolean handler(boolean expr, String[] params) {
        return serverRemoteService.getMenuCodePermission(params[0]);
    }

}
