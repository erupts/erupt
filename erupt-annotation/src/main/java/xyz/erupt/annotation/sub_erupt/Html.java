package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-10-16.
 */
public @interface Html {

    @Transient
    String path() default "";

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends HtmlHandler> htmlHandler() default HtmlHandler.class;

    interface HtmlHandler {

        /**
         * 获取模板绑定数据
         *
         * @param params 注解参数
         * @return 页面绑定数据
         */
        Map<String, Object> getData(String[] params);

    }

}
