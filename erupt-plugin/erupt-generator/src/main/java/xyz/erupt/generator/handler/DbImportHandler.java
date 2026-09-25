package xyz.erupt.generator.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.model.input.DbImportModal;
import xyz.erupt.generator.service.DbIntrospectService;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Turns the selected database tables into generator definitions.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class DbImportHandler implements OperationHandler<GeneratorClass, DbImportModal> {

    @Resource
    private DbIntrospectService dbIntrospectService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public DbImportModal eruptFormValue(List<GeneratorClass> data, DbImportModal modal, String[] param) {
        modal.setDataSource(dbIntrospectService.defaultDataSource());
        modal.setNamespace(dbIntrospectService.defaultNamespace(modal.getDataSource()));
        modal.setPackageName(defaultPackage());
        return modal;
    }

    /**
     * Where the generated classes belong: the package the models of this application already
     * live in, or the package of the boot class when there are none to learn from.
     */
    private static String defaultPackage() {
        Class<?> source = EruptApplication.getPrimarySource();
        if (null == source) return null;
        String base = source.getPackage().getName();
        return EruptCoreService.getErupts().stream()
                .map(it -> it.getClazz().getPackage().getName())
                .filter(it -> it.startsWith(base + "."))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                //the busiest package wins, the shortest of them settles a tie
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(it -> -it.getKey().length()))
                .map(Map.Entry::getKey).orElse(base + ".model");
    }

    @Override
    @Transactional
    public String exec(List<GeneratorClass> data, DbImportModal modal, String[] param) {
        for (GeneratorClass clazz : dbIntrospectService.introspect(modal)) {
            GeneratorClass exist = eruptDao.lambdaQuery(GeneratorClass.class)
                    .eq(GeneratorClass::getTableName, clazz.getTableName()).one();
            if (null != exist) {
                if (!Boolean.TRUE.equals(modal.getOverwrite())) continue;
                eruptDao.deleteAndFlush(exist);
            }
            eruptDao.persist(clazz);
        }
        return null;
    }

}
