package xyz.erupt.monitor;


import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/1/24 12:28
 */
public class ServerTest {

    @Test
    public void jvmParam() {
        System.out.println(System.getProperty("configurePath"));
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        System.out.println("Heap memory info: " + memoryBean.getHeapMemoryUsage());
        System.out.println("Non-heap memory info: " + memoryBean.getNonHeapMemoryUsage());
        List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        System.out.println("\n#####################JVM parameters set at runtime#######################");
        System.out.println(inputArgs);
    }

    @Test
    public void block() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.out.println("Heap memory info: " + memoryMXBean.getHeapMemoryUsage());
        System.out.println("Non-heap memory info: " + memoryMXBean.getNonHeapMemoryUsage());
    }

}
