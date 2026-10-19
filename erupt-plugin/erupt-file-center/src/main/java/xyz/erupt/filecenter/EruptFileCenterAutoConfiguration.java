package xyz.erupt.filecenter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.filecenter.web.FileCenterTpl;
import xyz.erupt.tpl.service.EruptTplService;

import java.util.ArrayList;
import java.util.List;

/**
 * The file center: one page that browses every store (the upload directory, extra folders, the
 * S3 bucket) in Finder-style columns, with previews. Seeing it is one menu; changing anything
 * (upload, new folder, rename, delete) is a button of its own, granted separately.
 *
 * @author YuePeng
 */
// @EruptScan: erupt-tpl finds @EruptTpl pages only in scanned packages
@Configuration
@ComponentScan
@EruptScan
public class EruptFileCenterAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptFileCenterAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-file-center").description("File center").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu page = MetaMenu.createSimpleMenu("file-center", "File Center", FileCenterTpl.PAGE, null, 53, EruptTplService.TPL);
        page.setIcon("fa fa-folder-open");
        menus.add(page);
        menus.add(MetaMenu.createSimpleMenu("file-center-write", "Write", FileCenterTpl.WRITE, page, 10, MenuTypeEnum.BUTTON.getCode()));
        return menus;
    }

}
