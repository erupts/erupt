package xyz.erupt.bi.handler;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.bi.constant.ChartTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/12/30 00:48
 */
public class ChartType implements ChoiceFetchHandler {

    public static final String TPL = "tpl";

    public static final String TABLE = "table";

    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> list = Arrays.stream(ChartTypeEnum.values()).map(it ->
                new VLModel(it.name(), it.getName(), it.getDesc())).collect(Collectors.toList());
        list.add(new VLModel(TABLE, "数据表", "返回任意列数"));
        list.add(new VLModel(TPL, "组件模板"));
        return list;
    }
}
