package xyz.erupt.filecenter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.filecenter.web.FileCenterController;

import java.util.ArrayList;
import java.util.List;

/**
 * The file center: the erupt-web page at /file-center browses every store (the upload directory,
 * extra folders, S3 buckets) in Finder-style columns, with previews, through this module's API.
 * Seeing it is one menu; changing anything (upload, new folder, rename, delete) is a button of
 * its own, granted separately.
 *
 * @author YuePeng
 */
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
        MetaMenu page = MetaMenu.createSimpleMenu("file-center", "File Center", FileCenterController.PAGE, null, 53, MenuTypeEnum.ROUTER.getCode());
        page.setIcon("fa fa-folder-open");
        menus.add(page);
        menus.add(MetaMenu.createSimpleMenu("file-center-write", "Write", FileCenterController.WRITE, page, 10, MenuTypeEnum.BUTTON.getCode()));
        return menus;
    }

}
