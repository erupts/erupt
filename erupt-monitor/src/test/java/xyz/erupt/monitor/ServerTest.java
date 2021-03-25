package xyz.erupt.monitor;

import org.junit.Test;

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
        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        System.out.println("堆内存信息: " + memorymbean.getHeapMemoryUsage());
        System.out.println("方法区内存信息: " + memorymbean.getNonHeapMemoryUsage());
        List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        System.out.println("\n#####################运行时设置的JVM参数#######################");
        System.out.println(inputArgs);

    }

    @Test
    public void block() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.out.println("堆内存信息: " + memoryMXBean.getHeapMemoryUsage());
        System.out.println("方法区内存信息: " + memoryMXBean.getNonHeapMemoryUsage());
    }
}
