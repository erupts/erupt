package xyz.erupt.cloud.server.annotation;

import jakarta.servlet.http.HttpServletRequest;
import xyz.erupt.cloud.server.node.MetaNode;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptCloudServer {

    Class<? extends Proxy> value();

    interface Proxy {

        /**
         * Node Heartbeat Registration
         *
         * @param metaNode Node registration object
         */
        default void registerNode(MetaNode metaNode, HttpServletRequest request) {

        }

        default void removeNode(String nodeName, HttpServletRequest request) {

        }

    }

}
