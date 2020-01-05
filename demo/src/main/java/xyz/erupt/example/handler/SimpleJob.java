//package xyz.erupt.example.demo.handler;
//
//import xyz.erupt.job.handler.JobHandler;
//
///**
// * @author liyuepeng
// * @date 2019-12-27.
// */
//public class SimpleJob implements JobHandler {
//
//    private static int i = 0;
//
//    @Override
//    public String exec(String param) throws Exception {
//        Thread.sleep(11100);
//        return String.valueOf(++i);
//    }
//
//    @Override
//    public void success(String result, String param) {
//        System.out.println(param + ":" + result);
//    }
//}
