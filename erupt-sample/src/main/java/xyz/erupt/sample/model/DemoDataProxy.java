package xyz.erupt.sample.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.Alert;
import xyz.erupt.annotation.query.Condition;

import java.util.List;

@Component
public class DemoDataProxy implements DataProxy<Demo> {

    @Override
    public void beforeUpdate(Demo demo) {
        DataProxy.super.beforeUpdate(demo);
    }

    @Override
    public void beforeAdd(Demo demo) {
        DataProxy.super.beforeAdd(demo);
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        return DataProxy.super.beforeFetch(conditions);
    }

    @Override
    public Alert alert(List<Condition> conditions) {
        return Alert.info("提示信息");
    }
}
