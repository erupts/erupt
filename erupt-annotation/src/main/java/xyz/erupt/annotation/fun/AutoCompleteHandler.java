package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-08-14
 */
public interface AutoCompleteHandler {

    List<Object> completeHandler(@Comment("前端其他表单组件的值") Map<String,Object> formData,
                                 @Comment("前端输入值") String val,
                                 @Comment("注解回传参数") String[] param);

}
