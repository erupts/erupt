package xyz.erupt.core.constant;

/**
 * Created by liyuepeng on 10/15/18.
 */
public class RestPath {

    //不做拦截URL前缀
    public static final String DONT_INTERCEPT = "/ws";

    public static final String ERUPT_API = "/erupt-api";

    //界面构建
    public static final String ERUPT_BUILD = ERUPT_API + "/build";

    //数据
    public static final String ERUPT_DATA = ERUPT_API + "/data";

    //excel
    public static final String ERUPT_EXCEL = ERUPT_API + "/excel";

    //文件上传
    public static final String ERUPT_FILE = ERUPT_API + "/file";
}
