package xyz.erupt.cloud.example.showcase;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import xyz.erupt.cloud.node.lite.Node;

/** The showcase on Solon: the node is a bean, which the erupt-cloud-node-solon plugin serves and connects. */
@SolonMain
public final class ShowcaseSolon {

    public static void main(String[] args) throws Exception {
        Node node = Showcase.node().factory(type -> Solon.context().getBean(type));
        Solon.start(ShowcaseSolon.class, new String[]{"--server.port=" + Showcase.port()}, app -> app.context().wrapAndPut(Node.class, node));
    }

    private ShowcaseSolon() {
    }
}
