package xyz.erupt.annotation.fun;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public interface OperationHandler<Target, EruptParam> {

    /**
     * 操作执行类
     *
     * @param data       待处理数据
     * @param eruptParam 处理参数
     * @param param      RowOperation注解参数
     */
    void exec(Target data, EruptParam eruptParam, String[] param);
}
