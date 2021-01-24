package xyz.erupt.server.vo;

/**
 * @author liyuepeng
 * @date 2021/1/23 23:47
 */
public class SysThread {

    public SysThread() {
        System.out.println("----------------线程信息----------------");
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        while (currentGroup.getParent() != null) {
            // 返回此线程组的父线程组
            currentGroup = currentGroup.getParent();
        }
        //此线程组中活动线程的估计数
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        //把对此线程组中的所有活动子组的引用复制到指定数组中。
        currentGroup.enumerate(lstThreads);
        for (Thread thread : lstThreads) {
            System.out.println("线程数量：" + noThreads + " 线程id：" + thread.getId() + " 线程名称：" + thread.getName() + " 线程状态：" + thread.getState());
        }
    }

}
