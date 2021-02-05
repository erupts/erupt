package xyz.erupt.jpa.model;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.rent.model.EruptRent;
import xyz.erupt.rent.service.RentDataLoadService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Service
public class BaseRentDataProxy implements DataProxy<BaseModel> {

    private static final Pattern IP_PATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    @Resource
    private HttpServletRequest request;

    @Override
    public void beforeAdd(BaseModel baseModel) {
        EruptRent eruptRent = findEruptRent();
        if (null != eruptRent) {
            baseModel.setEruptRent(eruptRent);
            baseModel.setRentToken(eruptRent.getToken());
        }
    }

    @Override
    public String beforeFetch() {
        EruptRent eruptRent = findEruptRent();
        if (null != eruptRent) {
            return EruptCoreService.getErupt(request.getHeader("erupt")).getEruptName() + ".eruptRent=" + eruptRent.getId();
        }
        return null;
    }

    //获取当前租户数据
    public EruptRent findEruptRent() {
        String serverName = request.getServerName();
        if (!isIP(serverName)) {
            EruptRent eruptRent = RentDataLoadService.getEruptRent(serverName);
            if (null == eruptRent) {
                return RentDataLoadService.getEruptRent(serverName.split("\\.")[0]);
            } else {
                if (EruptRent.MAPPING_SECOND_DOMAIN.equals(eruptRent.getMappingType())) {
                    return RentDataLoadService.getEruptRent(serverName.split("\\.")[0]);
                } else {
                    return eruptRent;
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
