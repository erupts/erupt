package xyz.erupt.report.handler;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.report.constant.ChartTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/12/30 00:48
 */
public class ChartType implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        return Arrays.stream(ChartTypeEnum.values()).map(it ->
                new VLModel(it.name(), it.getName(), it.getDesc())).collect(Collectors.toList());
    }
}
