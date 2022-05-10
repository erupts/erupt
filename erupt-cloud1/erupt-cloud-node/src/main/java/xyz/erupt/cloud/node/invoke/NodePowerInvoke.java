package xyz.erupt.cloud.node.invoke;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/2/20 01:06
 */
@Component
public class NodePowerInvoke implements PowerHandler {

    static {
        PowerInvoke.RegisterPowerHandler(NodePowerInvoke.class);
    }

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Override
    public void handler(PowerObject power) {
        EruptModel eruptModel = EruptCoreService.getErupt(MetaContext.getErupt().getName());
        if (!eruptModel.getErupt().authVerify()) {
            return;
        }
        String powerObjectString = HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.ERUPT_POWER)
                .form("nodeName", eruptNodeProp.getNodeName())
                .form("eruptName", eruptModel.getEruptName())
                .header(EruptMutualConst.TOKEN, MetaContext.getToken()).execute().body();
        BeanUtil.copyProperties(GsonFactory.getGson().fromJson(powerObjectString, PowerObject.class), power);
    }

}
