package xyz.demo.erupt.example;

import org.junit.Test;

import javax.script.ScriptEngineManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liyuepeng
 * @date 2020-09-14
 */
public class SimpleTest {

    @Test
    public void getProp() {
        System.out.println(System.getProperty("user.dir"));
    }

    @Test
    public void currScript() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        int max = 10000;
        CountDownLatch countDownLatch = new CountDownLatch(max);
        for (int i = 0; i < max; i++) {
            executorService.submit(() -> {
                countDownLatch.countDown();
                new ScriptEngineManager().getEngineByName("js");
            });
        }
        countDownLatch.await();
        System.out.println(countDownLatch.getCount());
    }

}
