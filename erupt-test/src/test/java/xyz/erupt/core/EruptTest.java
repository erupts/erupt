package xyz.erupt.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.EruptApplicationTests;
import xyz.erupt.core.module.EruptModuleInvoke;

public class EruptTest extends EruptApplicationTests {

    @Test
    void modules() {
        StringBuilder sb = new StringBuilder();
        EruptModuleInvoke.invoke(it -> sb.append(it.info().getName()).append(": ").append(it.info().getDescription()).append("\n"));
        System.out.println("Erupt Modules: \n" + sb);
    }

}
