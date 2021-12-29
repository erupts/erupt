package xyz.erupt.bi.handler;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/12/30 00:48
 */
public class ChartType implements ChoiceFetchHandler {

    public static final String TPL = "tpl";

    public static final String TABLE = "table";

    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> list = new ArrayList<>();
        list.add(new VLModel(TPL,"自定义模板","使用前请确认是否导入erupt-tpl模块"));
        list.add(new VLModel(TABLE,"数据表","返回任意列数"));
        return list;
    }
}
