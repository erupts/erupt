package xyz.erupt.k8s_dashboard;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.k8s_dashboard.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Ready-made Kubernetes admin on top of erupt-data-k8s. Adding the dependency is the whole setup:
 * the models read the cluster of the standard client chain ({@code KUBECONFIG}, {@code ~/.kube/config},
 * then the in-cluster service account) and arrive with their menus.
 *
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EruptScan
public class EruptK8sDashboardAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptK8sDashboardAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-k8s-dashboard").description("Kubernetes dashboard").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu("$k8s", "Kubernetes", "fa fa-dharmachakra", 57);
        menus.add(root);
        int sort = 0;
        MetaMenu pod = MetaMenu.createEruptClassMenu(KubePod.class, root, sort += 10);
        menus.add(pod);
        // Opening a shell is a grant of its own: seeing pods does not imply exec into them
        menus.add(MetaMenu.createSimpleMenu(KubePod.TERMINAL, "Terminal", KubePod.TERMINAL, pod, 10, MenuTypeEnum.BUTTON.getCode()));
        for (Class<?> model : List.of(KubeDeployment.class, KubeStatefulSet.class, KubeDaemonSet.class, KubeService.class,
                KubeConfigMap.class, KubeEvent.class, KubeNamespace.class, KubeNode.class)) {
            menus.add(MetaMenu.createEruptClassMenu(model, root, sort += 10));
        }
        return menus;
    }

}
