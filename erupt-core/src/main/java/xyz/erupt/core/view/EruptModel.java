package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.EruptProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.CloneSupport;

import java.util.List;
import java.util.Map;
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

    private JsonObject eruptJson;

    private List<EruptFieldModel> eruptFieldModels;

    //默认查询条件
    private Map<String, Object> searchCondition;

    private boolean extraRow = false;

    public EruptModel(Class<?> eruptClazz) {
        this.clazz = eruptClazz;
        this.erupt = eruptClazz.getAnnotation(Erupt.class);
        this.erupt = eruptAnnotationProxy.newProxy(this.getErupt());
        this.eruptName = eruptClazz.getSimpleName();
        this.i18n = null != clazz.getAnnotation(EruptI18n.class);
        DataProxyInvoke.invoke(this, it -> {
            try {
                it.getClass().getDeclaredMethod("extraRow", List.class);
                this.extraRow = true;
            } catch (NoSuchMethodException ignored) {
            }
        });
    }

    public Erupt getErupt() {
        ProxyContext.set(clazz);
        return erupt;
    }

    @Override
    public EruptModel clone() throws CloneNotSupportedException {
        EruptModel eruptModel = (EruptModel) super.clone();
        eruptModel.eruptJson = AnnotationProcess.annotationToJsonByReflect(this.getErupt());
        eruptModel.eruptFieldModels = eruptFieldModels.stream().map(CloneSupport::clone)
                .peek(EruptFieldModel::serializable).collect(Collectors.toList());
        return eruptModel;
    }

}
