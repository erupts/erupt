package xyz.erupt.bi;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.model.*;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
@EnableConfigurationProperties
public class EruptBiAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptBiAutoConfiguration.class);
    }

    @Resource
    private EruptDao eruptDao;

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-bi").build();
    }

    @Override
    public void run() {
        MenuTypeEnum.addMenuType(new VLModel("bi", "报表", "报表编码"));
    }

    @Override
    @SneakyThrows
    public void initFun() {
        String code = "BI_FUN";
        eruptDao.persistIfNotExist(BiFunction.class, new BiFunction(code, code,
                StreamUtils.copyToString(EruptBiAutoConfiguration.class.getResourceAsStream("BiDefaultFunction.js")
                        , StandardCharsets.UTF_8)), "code", code);
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$mbi", "在线报表", "fa fa-table", 20));
        menus.add(MetaMenu.createEruptClassMenu(BiDataSource.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(BiClassHandler.class, menus.get(0), 20));
        menus.add(MetaMenu.createEruptClassMenu(BiTpl.class, menus.get(0), 30));
        menus.add(MetaMenu.createEruptClassMenu(BiDimensionReference.class, menus.get(0), 40));
        menus.add(MetaMenu.createEruptClassMenu(BiFunction.class, menus.get(0), 50));
        menus.add(MetaMenu.createEruptClassMenu(BiGroup.class, menus.get(0), 60, MenuTypeEnum.TREE));
        MetaMenu bi = MetaMenu.createEruptClassMenu(Bi.class, menus.get(0), 100);
        menus.add(bi);
        menus.add(MetaMenu.createEruptClassMenu(BiChart.class, bi, 0, MenuStatus.HIDE));
        menus.add(MetaMenu.createEruptClassMenu(BiHistory.class, bi, 0, MenuStatus.HIDE));
        return menus;
    }

}
