package xyz.erupt.monitor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.monitor.model.EruptClassInfo;
import xyz.erupt.upms.model.EruptMenu;

import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory data source for the erupt class registry page: rows are built from
 * EruptCoreService on every query so runtime-registered models show up instantly.
 *
 * @author YuePeng
 */
@Service
public class EruptClassInfoDataService extends EruptBeanDataService<EruptClassInfo> {

    public static final String DATA_PROCESSOR = "ERUPT_CLASS_INFO";

    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    // Trailing build-output segments stripped when resolving a class source from a directory path
    private static final Set<String> BUILD_DIRS = new HashSet<>(Arrays.asList(
            "classes", "target", "build", "out", "main", "java", "production", "bin"));

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptClassInfoDataService.class);
    }

    @Resource
    private EruptDao eruptDao;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptClassInfo info = (EruptClassInfo) super.findDataById(eruptModel, id);
        if (null != info) {
            Optional.ofNullable(EruptCoreService.getErupt(info.getName())).ifPresent(it ->
                    info.setJson(PRETTY_GSON.toJson(AnnotationProcess.annotationToJsonByReflect(it.getErupt()))));
        }
        return info;
    }

    @Override
    protected List<EruptClassInfo> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        Set<String> menuValues = eruptDao.lambdaQuery(EruptMenu.class).list().stream()
                .map(EruptMenu::getValue).filter(Objects::nonNull).collect(Collectors.toSet());
        List<EruptClassInfo> list = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            EruptClassInfo info = new EruptClassInfo();
            info.setName(model.getEruptName());
            info.setDisplayName(model.getErupt().name());
            info.setClazz(model.getClazz().getName());
            info.setSource(this.resolveSource(model.getClazz()));
            info.setI18n(model.isI18n());
            info.setFieldCount(model.getEruptFieldModels().size());
            EruptDataProcessor processor = model.getClazz().getAnnotation(EruptDataProcessor.class);
            info.setDataProcessor(null == processor ? EruptConst.DEFAULT_DATA_PROCESSOR : processor.value());
            info.setRuntime(EruptCoreService.isRuntimeErupt(model.getEruptName()));
            info.setPublished(menuValues.contains(model.getEruptName()));
            list.add(info);
        }
        return list;
    }

    private String resolveSource(Class<?> clazz) {
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (null == codeSource || null == codeSource.getLocation()) return null;
            String path = codeSource.getLocation().getPath().replace('\\', '/');
            // nested boot jar paths keep the innermost jar last, e.g. app.jar!/BOOT-INF/lib/erupt-upms.jar!/
            int jar = path.lastIndexOf(".jar");
            if (jar > -1) {
                String head = path.substring(0, jar);
                return head.substring(head.lastIndexOf('/') + 1) + ".jar";
            }
            String[] segments = Arrays.stream(path.split("/")).filter(it -> !it.isEmpty()).toArray(String[]::new);
            int end = segments.length;
            while (end > 0 && BUILD_DIRS.contains(segments[end - 1])) end--;
            return end > 0 ? segments[end - 1] : null;
        } catch (Exception e) {
            return null;
        }
    }

}
