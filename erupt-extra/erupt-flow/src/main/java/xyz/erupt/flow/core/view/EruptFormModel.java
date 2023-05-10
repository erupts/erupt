package xyz.erupt.flow.core.view;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.view.EruptModel;

@Getter
@Setter
public class EruptFormModel {

    private String name;
    //表单项
    private JSONArray formItems;

    public EruptFormModel(EruptModel eruptModel) {
        this.name = eruptModel.getErupt().name();//注解的名字
    }

}
