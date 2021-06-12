package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2020-08-14
 */
public interface AutoCompleteHandler {

    List<Object> completeHandler(@Comment("前端输入值") String val, @Comment("注解回传参数") String[] param);

}
