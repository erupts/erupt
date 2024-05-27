package xyz.erupt.upms.model.filter;

import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.core.constant.MenuTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/5/27 21:39
 */
public class EruptMenuViewFilter implements FilterHandler {

    @Override
    public String filter(String condition, String[] params) {
        List<String> nts = new ArrayList<>();
        nts.add(MenuTypeEnum.API.getCode());
        nts.add(MenuTypeEnum.BUTTON.getCode());
        return String.format("EruptMenu.type not in ('%s') or EruptMenu.type is null", String.join("','", nts));
    }

}
