package xyz.erupt.core.handler;

import xyz.erupt.annotation.fun.ConditionHandler;
import xyz.erupt.annotation.model.PlaceholderData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class SimpleConditionHandler implements ConditionHandler {

    @Override
    public List<PlaceholderData> handler() {
        List<PlaceholderData> placeholderDataList = new ArrayList<>();
        placeholderDataList.add(new PlaceholderData("abc", "1"));
        return placeholderDataList;
    }
}
