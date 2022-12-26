package xyz.erupt.cloud.node.service;

import cn.hutool.http.HttpUtil;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.module.MetaUserinfo;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/3/9 20:11
 */
@Service
public class ServerRemoteService {

    @Resource
    private EruptNodeProp eruptNodeProp;

    //校验菜单权限
    public boolean getMenuCodePermission(String menuValue) {
        String permissionResult =
                HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + EruptRestPath.ERUPT_CODE_PERMISSION + "/" + menuValue)
                        .header(EruptMutualConst.TOKEN, MetaContext.getToken()).execute().body();
        return Boolean.parseBoolean(permissionResult);
    }

    public MetaUserinfo getRemoteUserInfo() {
        String userinfo = HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.ERUPT_USER_INFO + eruptNodeProp.getNodeName())
                .header(EruptMutualConst.TOKEN, MetaContext.getToken())
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .execute().body();
        return GsonFactory.getGson().fromJson(userinfo, MetaUserinfo.class);
    }

    public String getNodeConfig() {
        return HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.NODE_CONFIG + "/" + eruptNodeProp.getNodeName())
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .execute().body();
    }

    public String getNodeGroupConfig() {
        return HttpUtil.createGet(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.NODE_GROUP_CONFIG + "/" + eruptNodeProp.getNodeName())
                .form(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .execute().body();
    }


}
