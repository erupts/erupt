package xyz.erupt.flow.process.listener;

/**
 * 通用的流程监听器
 * 适用于 实例，线程，活动，任务 这些可执行的东西
 * 这些监听器必须要继承这个监听器
 */
public interface ExecutableNodeListener<T> extends Comparable<ExecutableNodeListener> {

    /**
     * 默认顺序，最大
     * @return
     */
    default int sort() {
        return Integer.MIN_VALUE;
    }

    @Override
    default int compareTo(ExecutableNodeListener to)  {
        //比较优先级值越小越靠前
        return to.sort() - this.sort();
    }

    /**
     * 执行监听，如果某个监听返回值为false，则中断监听链
     * @param executableNode
     */
    void execute(T executableNode);
}
