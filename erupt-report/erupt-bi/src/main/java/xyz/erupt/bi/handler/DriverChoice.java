package xyz.erupt.bi.handler;

import org.apache.commons.collections4.iterators.EnumerationIterator;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/12/28 21:33
 */
public class DriverChoice implements ChoiceFetchHandler {

    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> list = new ArrayList<>();
        for (EnumerationIterator<Driver> it = new EnumerationIterator<>(DriverManager.getDrivers()); it.hasNext(); ) {
            String name = it.next().getClass().getName();
            if (!name.startsWith("com.alibaba.druid")) {
                list.add(new VLModel(name, name));
            }
        }
        return list;
    }

}
