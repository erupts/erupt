package xyz.erupt.tenant.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.tenant.model.EruptTenant;
import xyz.erupt.upms.constant.MenuTypeEnum;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class RentDataLoadService implements CommandLineRunner, DataProxy<EruptTenant> {

    private static final Map<String, EruptTenant> eruptRentMap = new HashMap<>();
    private static final Map<String, String> rentUrlMappingMap = new HashMap<>();

    static {
        try {
            EruptField eruptField = EruptTenant.class.getDeclaredField("mappingType").getAnnotation(EruptField.class);
            for (VL vl : eruptField.edit().choiceType().vl()) {
                rentUrlMappingMap.put(vl.label(), vl.value());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Resource
    private EruptDao eruptDao;
    @Resource
    private HttpServletRequest request;

    public static EruptTenant getEruptRent(String link) {
        return eruptRentMap.get(link);
    }

    private void fillRentMap() {
        eruptRentMap.clear();
        for (EruptTenant rent : eruptDao.queryEntityList(EruptTenant.class, "status = true")) {
            eruptRentMap.put(rent.getMappingValue(), rent);
        }
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        this.fillRentMap();
        new ProjectUtil().projectStartLoaded("rent", first -> {
            if (first) {
                String code = "code";
                String manager = "$manager";
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(manager, "系统管理", null, null, 1, 0, "fa fa-cogs", null)
                        , code, manager);
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(EruptTenant.class.getSimpleName(), "租户管理", MenuTypeEnum.TABLE.getCode(), EruptTenant.class.getSimpleName()
                        , Integer.valueOf(EruptMenu.OPEN), 1, null, eruptMenu), code, EruptTenant.class.getSimpleName());
            }
        });
    }

    @Override
    public void beforeAdd(EruptTenant eruptTenant) {
        eruptTenant.setToken(RandomStringUtils.randomAlphanumeric(6));
    }

    @Override
    public void afterAdd(EruptTenant eruptTenant) {
        this.fillRentMap();
    }

    @Override
    public void afterUpdate(EruptTenant eruptTenant) {
        this.fillRentMap();
    }

    @Override
    public void afterDelete(EruptTenant eruptTenant) {
        this.fillRentMap();
    }

    @SneakyThrows
    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            String mappingType = rentUrlMappingMap.get(map.get("mappingType"));
            String mappingValue = (String) map.get("mappingValue");
            if (EruptTenant.MAPPING_DOMAIN.equals(mappingType)) {
                map.put("mappingValue", mappingValue);
            } else if (EruptTenant.MAPPING_SECOND_DOMAIN.equals(mappingType)) {
                map.put("mappingValue", mappingValue + "." + request.getServerName());
            }
        }
    }
}
