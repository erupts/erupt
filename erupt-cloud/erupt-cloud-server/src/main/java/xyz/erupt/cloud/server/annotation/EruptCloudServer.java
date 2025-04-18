package xyz.erupt.cloud.server.annotation;

import xyz.erupt.cloud.server.node.MetaNode;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptCloudServer {

    Class<? extends Proxy> value();

    interface Proxy {

        /**
         * 节点心跳注册
         *
         * @param metaNode 节点注册对象
         * @param request  request对象
         */
        default void registerNode(MetaNode metaNode, HttpServletRequest request) {

        }

        /**
         * 节点移除
         *
         * @param nodeName 节点名
         * @param request  request对象
         */
        default void removeNode(String nodeName, HttpServletRequest request) {

        }

    }

}
