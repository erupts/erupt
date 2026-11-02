package xyz.erupt.k8s.service;

import xyz.erupt.k8s.annotation.EruptK8s;

/**
 * Picks the kubeconfig context a model talks to at call time, for applications that switch
 * clusters per request (erupt-k8s-dashboard does it from the scope the user picked). Beans are
 * asked in order; the first non-null answer wins, and none means the kubeconfig's current context.
 *
 * @author YuePeng
 */
public interface K8sContextResolver {

    String context(EruptK8s eruptK8s);

}
