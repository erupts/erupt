package xyz.erupt.annotation.sub_erupt;

import java.util.Map;

/**
 * Created by liyuepeng on 2019-10-16.
 */
public @interface Html {

    String path() default "";

    String[] params() default {};

    Class<? extends HtmlHandler> htmlHandler() default HtmlHandler.class;

    interface HtmlHandler {

        Map<String, Object> getData(String[] params);

    }

}
