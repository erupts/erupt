package xyz.erupt.k8s_dashboard.scope;

import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The clusters to pick from: every context of the kubeconfig the dashboard reads, its current one
 * first so it becomes the default. A dashboard pinned to a master URL, or running in-cluster without
 * a kubeconfig, offers its one cluster under a fixed value.
 *
 * @author YuePeng
 */
@Component
public class KubeContextFetchHandler implements ChoiceFetchHandler<KubeScope> {

    // Values that name no kubeconfig context: the client connects the way it is configured
    public static final String CONFIGURED = "configured";

    public static final String IN_CLUSTER = "in-cluster";

    private final Environment environment;

    public KubeContextFetchHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        String masterUrl = environment.getProperty("erupt.k8s-dashboard.master-url", "");
        String kubeconfig = environment.getProperty("erupt.k8s-dashboard.kubeconfig", "");
        if (StringUtils.isNotBlank(masterUrl) && StringUtils.isBlank(kubeconfig)) {
            return List.of(new VLModel(CONFIGURED, masterUrl));
        }
        Config config;
        try {
            config = StringUtils.isBlank(kubeconfig) ? Config.autoConfigure(null)
                    : Config.fromKubeconfig(null, Files.readString(Path.of(kubeconfig)), kubeconfig);
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
        List<NamedContext> contexts = null == config.getContexts() ? List.of() : config.getContexts();
        if (contexts.isEmpty()) return List.of(new VLModel(IN_CLUSTER, IN_CLUSTER));
        String current = null == config.getCurrentContext() ? null : config.getCurrentContext().getName();
        List<VLModel> options = new ArrayList<>();
        for (NamedContext context : contexts) {
            VLModel option = new VLModel(context.getName(), context.getName());
            if (context.getName().equals(current)) options.add(0, option);
            else options.add(option);
        }
        return options;
    }

    // The context to hand the client: null for the values that name none
    public static String context(String cluster) {
        return null == cluster || CONFIGURED.equals(cluster) || IN_CLUSTER.equals(cluster) ? null : cluster;
    }

}
