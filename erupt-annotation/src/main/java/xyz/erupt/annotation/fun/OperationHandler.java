package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface OperationHandler<@Comment("行数据类型") Target, @Comment("表单输入对象类型") EruptObjParam> {

    @Comment("按钮事件触发类")
    void exec(@Comment("行数据") List<Target> data,
              @Comment("表单输入数据") EruptObjParam eruptObjParam,
              @Comment("注解回传参数") String[] param);

}
