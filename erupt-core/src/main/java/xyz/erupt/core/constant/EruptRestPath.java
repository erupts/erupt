package xyz.erupt.core.constant;

/**
 * @author YuePeng
 * date 10/15/18.
 */
public class EruptRestPath {

    public static final String ERUPT_API = "/erupt-api";

    //界面构建
    public static final String ERUPT_BUILD = ERUPT_API + "/build";

    //数据
    public static final String ERUPT_DATA = ERUPT_API + "/data";

    //数据增删改操作
    public static final String ERUPT_DATA_MODIFY = ERUPT_DATA + "/modify";
    public static final String ERUPT_DATA_UPDATE = ERUPT_DATA + "/update";
    public static final String ERUPT_DATA_DELETE = ERUPT_DATA + "/delete";

    //组件
    public static final String ERUPT_COMP = ERUPT_API + "/comp";

    //excel
    public static final String ERUPT_EXCEL = ERUPT_API + "/excel";

    //文件
    public static final String ERUPT_FILE = ERUPT_API + "/file";

    //附件虚拟路径
    public static final String ERUPT_ATTACHMENT = "/erupt-attachment";

    //菜单类型值权限
    public static final String ERUPT_CODE_PERMISSION = ERUPT_API + "/erupt-permission";

}
