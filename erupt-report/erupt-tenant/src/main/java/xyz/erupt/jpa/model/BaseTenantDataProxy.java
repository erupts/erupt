package xyz.erupt.jpa.model;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.tenant.model.EruptTenant;
import xyz.erupt.tenant.service.RentDataLoadService;
import xyz.erupt.upms.service.EruptContextService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Service
public class BaseTenantDataProxy implements DataProxy<BaseModel> {

    private static final Pattern IP_PATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptContextService eruptContextService;

    @Override
    public void beforeAdd(BaseModel baseModel) {
        EruptTenant eruptTenant = findEruptRent();
        if (null != eruptTenant) {
            baseModel.setEruptTenant(eruptTenant);
            baseModel.setTenantToken(eruptTenant.getToken());
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        EruptTenant eruptTenant = findEruptRent();
        if (null != eruptTenant) {
            return eruptContextService.getContextEruptClass().getSimpleName() + ".eruptTenant=" + eruptTenant.getId();
        }
        return null;
    }

    //获取当前租户数据
    public EruptTenant findEruptRent() {
        String serverName = request.getServerName();
        if (!isIP(serverName)) {
            EruptTenant eruptTenant = RentDataLoadService.getEruptRent(serverName);
            if (null == eruptTenant) {
                return RentDataLoadService.getEruptRent(serverName.split("\\.")[0]);
            } else {
                if (EruptTenant.MAPPING_SECOND_DOMAIN.equals(eruptTenant.getMappingType())) {
                    return RentDataLoadService.getEruptRent(serverName.split("\\.")[0]);
                } else {
                    return eruptTenant;
                }
            }
        }
        return null;
    }

    public boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15) {
            return false;
        }
        return IP_PATTERN.matcher(addr).find();
    }
}
