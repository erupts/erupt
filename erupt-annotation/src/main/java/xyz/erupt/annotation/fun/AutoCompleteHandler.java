package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2020-08-14
 */
public interface AutoCompleteHandler<MODEL> {

    List<Object> completeHandler(@Comment("values of other frontend form components") MODEL model,
                                 @Comment("frontend input value") String val,
                                 @Comment("annotation callback parameters") String[] param);

}
