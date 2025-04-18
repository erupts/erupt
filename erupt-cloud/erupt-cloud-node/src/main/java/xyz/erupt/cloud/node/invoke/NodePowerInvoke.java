package xyz.erupt.cloud.node.invoke;

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

import jakarta.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/2/20 01:06
 */
@Component
public class NodePowerInvoke implements PowerHandler {

    static {
        PowerInvoke.registerPowerHandler(NodePowerInvoke.class);
    }

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Override
    public void handler(PowerObject power) {
        EruptModel eruptModel = EruptCoreService.getErupt(MetaContext.getErupt().getName());
        PowerObject remotePowerObject = GsonFactory.getGson().fromJson(HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.ERUPT_POWER)
                .form("nodeName", eruptNodeProp.getNodeName()).form("eruptName", eruptModel.getEruptName())
                .header(EruptMutualConst.TOKEN, MetaContext.getToken()).execute().body(), PowerObject.class);
        if (power.isAdd()) power.setAdd(remotePowerObject.isAdd());
        if (power.isDelete()) power.setDelete(remotePowerObject.isDelete());
        if (power.isEdit()) power.setEdit(remotePowerObject.isEdit());
        if (power.isViewDetails()) power.setViewDetails(remotePowerObject.isViewDetails());
        if (power.isExport()) power.setExport(remotePowerObject.isExport());
        if (power.isImportable()) power.setImportable(remotePowerObject.isImportable());
    }

}
