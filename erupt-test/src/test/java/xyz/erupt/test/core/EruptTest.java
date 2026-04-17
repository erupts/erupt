package xyz.erupt.test.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.test.EruptApplicationTests;

public class EruptTest extends EruptApplicationTests {

    @Test
    void modules() {
        StringBuilder sb = new StringBuilder();
        EruptModuleInvoke.invoke(it -> sb.append(it.info().getName()).append(": ").append(it.info().getDescription()).append("\n"));
        System.out.println("Erupt Modules: \n" + sb);
    }

}
