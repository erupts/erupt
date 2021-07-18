package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface OperationHandler<@Comment("行数据类型") Target, @Comment("表单输入对象类型") EruptObject> {

    @Comment("按钮事件触发类")
    @Comment("返回值为JavaScript表达式，可交给前端执行")
    String exec(@Comment("行数据") List<Target> data,
              @Comment("表单输入数据") EruptObject eruptObject,
              @Comment("注解回传参数") String[] param);

//    @Comment("事件触发后执行 JavaScript 表达式")
//    default String afterJS(List<Target> data, EruptObject eruptObject, String[] param) {
//        return null;
//    }

}
