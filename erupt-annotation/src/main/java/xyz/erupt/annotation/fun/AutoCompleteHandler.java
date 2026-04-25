package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-08-14
 */
public interface AutoCompleteHandler {

    List<Object> completeHandler(@Comment("values of other frontend form components") Map<String,Object> formData,
                                 @Comment("frontend input value") String val,
                                 @Comment("annotation callback parameters") String[] param);

}
