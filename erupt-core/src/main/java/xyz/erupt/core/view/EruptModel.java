package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.EruptTag;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.manager.EruptUiAnnotationManager;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.EruptProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.CloneSupport;
import xyz.erupt.linq.lambda.LambdaSee;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2018-09-29.
 */
@Getter
@Setter
public class EruptModel implements Cloneable {

    private transient Class<?> clazz;

    private transient Erupt erupt;

    private transient AnnotationProxy<Erupt, Void> eruptAnnotationProxy = new EruptProxy();

    private transient Map<String, EruptFieldModel> eruptFieldMap;

    private transient boolean i18n;

    private String eruptName;

    // erupt-cloud: the node this erupt physically lives on; null means local
    private transient String remoteNode;

    private JsonObject eruptJson;

    private List<EruptFieldModel> eruptFieldModels;

    // Default search condition
    private Map<String, Object> searchCondition;

    private boolean extraRow = false;

    private Map<String, JsonObject> tags = new HashMap<>();

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.erupt = eruptAnnotationProxy.newProxy(this.getErupt());
        this.eruptName = eruptClazz.getSimpleName();
        this.i18n = null != clazz.getAnnotation(EruptI18n.class);
        for (Annotation annotation : eruptClazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().getAnnotation(EruptTag.class) != null) {
                tags.put(annotation.annotationType().getSimpleName(), AnnotationProcess.annotationToJsonByReflect(annotation));
            }
        }
        DataProxyInvoke.invoke(this, it -> {
            try {
                it.getClass().getDeclaredMethod(LambdaSee.field(EruptModel::isExtraRow), List.class);
                this.extraRow = true;
            } catch (NoSuchMethodException ignored) {
            }
        });
    }

    // Lightweight placeholder for a remote (erupt-cloud node) erupt that has no local class.
    // 'erupt' is a dynamic proxy returning annotation defaults, so legacy callers that only
    // read getErupt() (power, authVerify, etc.) keep working instead of throwing NPE.
    public EruptModel(String eruptName, String remoteNode) {
        this.eruptName = eruptName;
        this.remoteNode = remoteNode;
        this.erupt = (Erupt) Proxy.newProxyInstance(Erupt.class.getClassLoader(), new Class[]{Erupt.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "name" -> eruptName;
                    case "annotationType" -> Erupt.class;
                    case "toString" -> "@" + Erupt.class.getName() + "(remote:" + eruptName + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> method.getDefaultValue();
                });
    }

    public boolean isRemote() {
        return null != remoteNode;
    }

    public Erupt getErupt() {
        // Remote placeholder has no local class; its proxied annotation needs no i18n context
        if (null != clazz) ProxyContext.set(clazz);
        return erupt;
    }

    @Override
    public EruptModel clone() throws CloneNotSupportedException {
        EruptModel eruptModel = (EruptModel) super.clone();
        eruptModel.eruptJson = AnnotationProcess.annotationToJsonByReflect(this.getErupt());
        JsonObject extra = new JsonObject();
        eruptModel.eruptJson.add(LambdaSee.method(Erupt::extra), extra);
        EruptUiAnnotationManager.getAnnotations().forEach(annotation -> Optional.ofNullable(this.getClazz().getAnnotation(annotation))
                .ifPresent(it -> extra.add(annotation.getSimpleName(), AnnotationProcess.annotationToJsonByReflect(it))));
        for (Class<? extends Annotation> ex : this.getErupt().extra()) {
            Optional.ofNullable(this.getClazz().getAnnotation(ex)).ifPresent(it -> extra.add(ex.getSimpleName(), AnnotationProcess.annotationToJsonByReflect(it)));
        }
        eruptModel.eruptFieldModels = eruptFieldModels.stream().map(CloneSupport::clone).peek(EruptFieldModel::serializable).collect(Collectors.toList());
        return eruptModel;
    }

}
