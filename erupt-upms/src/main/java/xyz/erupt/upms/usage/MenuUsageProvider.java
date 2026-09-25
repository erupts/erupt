package xyz.erupt.upms.usage;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.usage.EruptUsage;
import xyz.erupt.core.usage.EruptUsageProvider;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A menu is where a model becomes something a user can open. Nothing on the model says so, and
 * renaming the model leaves the menu pointing at a name nobody answers to.
 *
 * @author YuePeng
 */
@Service
public class MenuUsageProvider implements EruptUsageProvider {

    // The three menu types whose value is an erupt name; everything else carries a url or a code
    private static final Set<String> MODEL_MENUS = Set.of(MenuTypeEnum.TABLE.getCode(),
            MenuTypeEnum.TREE.getCode(), MenuTypeEnum.FORM.getCode());

    @Resource
    private EruptDao eruptDao;

    @Resource
    private I18nTranslate i18nTranslate;

    @Override
    public List<EruptUsage> usages() {
        // One language for the whole sweep: a menu name is translated the same way the navigation
        // translates it, or the row would read in English next to a console that does not
        String lang = i18nTranslate.currentLang();
        String usage = i18nTranslate.translate(lang, "Menu display");
        List<EruptUsage> usages = new ArrayList<>();
        for (EruptMenu menu : eruptDao.lambdaQuery(EruptMenu.class).list()) {
            if (null != menu.getValue() && MODEL_MENUS.contains(menu.getType())) {
                usages.add(new EruptUsage(menu.getValue(), usage, i18nTranslate.translate(lang, menu.getName()),
                        EruptUsage.tableRoute(EruptMenu.class)));
            }
        }
        return usages;
    }

}
