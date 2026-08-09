package xyz.erupt.ai_canvas.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.EruptAiCanvasAutoConfiguration;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.model.AiCanvasMenuModal;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptMenuService;

import java.util.List;

/**
 * Publishes an AI Canvas page to the navigation menu as an aiCanvas-typed
 * entry whose value is the canvas code.
 *
 * @author YuePeng
 * date 2026/8/9
 */
@Component
public class AiCanvasMenuHandler implements OperationHandler<AiCanvas, AiCanvasMenuModal> {

    private static final String MENU_CODE_PREFIX = "ai-canvas-";

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptMenuService eruptMenuService;

    @Override
    @Transactional
    public String exec(List<AiCanvas> data, AiCanvasMenuModal modal, String[] param) {
        AiCanvas canvas = data.get(0);
        String menuCode = MENU_CODE_PREFIX + canvas.getCode();
        Erupts.requireNull(
                eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, menuCode).one(),
                I18nTranslate.$translate("ai-canvas.menu_exists")
        );
        Integer max = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        eruptDao.persist(new EruptMenu(
                menuCode, modal.getName(), EruptAiCanvasAutoConfiguration.MENU_TYPE,
                canvas.getCode(),
                MenuStatus.OPEN.getValue(), (max == null ? 0 : max) + 10,
                null, modal.getEruptMenu()
        ));
        eruptMenuService.flushMenuCache();
        return null;
    }

    @Override
    public AiCanvasMenuModal eruptFormValue(List<AiCanvas> data, AiCanvasMenuModal modal, String[] param) {
        modal.setName(data.get(0).getName());
        return modal;
    }

}
