package xyz.erupt.cloud.node.service;

import xyz.erupt.cloud.common.http.CloudHttp;
import org.springframework.web.client.RestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.module.MetaUserinfo;

/**
 * @author YuePeng
 * date 2022/3/9 20:11
 */
@Service
public class ServerRemoteService {

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Resource
    private RestClient serverRestClient;

    // Verify menu permissions
    public boolean getMenuCodePermission(String menuValue) {
        String permissionResult = serverRestClient.get().uri(eruptNodeProp.getBalanceAddress() + EruptRestPath.ERUPT_CODE_PERMISSION + "/" + menuValue)
                .headers(CloudHttp.header(EruptMutualConst.TOKEN, MetaContext.getToken()))
                .retrieve().body(String.class);
        return Boolean.parseBoolean(permissionResult);
    }

    public MetaUserinfo getRemoteUserInfo() {
        String userinfo = serverRestClient.get().uri(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.ERUPT_USER_INFO + "/" + eruptNodeProp.getNodeName())
                .headers(CloudHttp.header(EruptMutualConst.TOKEN, MetaContext.getToken()))
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .retrieve().body(String.class);
        return GsonFactory.getGson().fromJson(userinfo, MetaUserinfo.class);
    }

    public String getNodeConfig() {
        return serverRestClient.get().uri(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.NODE_CONFIG + "/" + eruptNodeProp.getNodeName())
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .retrieve().body(String.class);
    }

    public String getNodeGroupConfig() {
        return serverRestClient.get().uri(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.NODE_GROUP_CONFIG + "/" + eruptNodeProp.getNodeName(),
                        builder -> builder.queryParam(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken()).build())
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, eruptNodeProp.getAccessToken())
                .retrieve().body(String.class);
    }

}
