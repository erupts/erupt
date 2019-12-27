package xyz.erupt.example.demo.handler;

import xyz.erupt.job.handler.JobHandler;

/**
 * @author liyuepeng
 * @date 2019-12-27
 */
public class SimpleJob implements JobHandler {

    private static int i = 0;

    @Override
    public void exec(String param) throws Exception {
        System.out.println(++i);
    }

    @Override
    public void complete(boolean success, String param) {

    }
}
