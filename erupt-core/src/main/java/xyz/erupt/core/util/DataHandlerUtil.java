package xyz.erupt.core.util;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceEnum;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.TreeModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 2019-04-28.
 */
public class DataHandlerUtil {
    //内存计算的方式生成树结构
    public static List<TreeModel> treeModelToTree(List<TreeModel> treeModels) {
        List<TreeModel> resultTreeModels = new ArrayList<>();
        List<TreeModel> tempTreeModels = new LinkedList<>();
        tempTreeModels.addAll(treeModels);
        for (TreeModel treeModel : treeModels) {
            if (StringUtils.isBlank(treeModel.getPid())) {
                resultTreeModels.add(treeModel);
                tempTreeModels.remove(treeModel);
            }
        }
        for (TreeModel treeModel : resultTreeModels) {
            recursionTree(tempTreeModels, treeModel);
        }
        //TODO 如果最终结果size为0则直接返回原有参数
        return resultTreeModels;
    }

    private static void recursionTree(List<TreeModel> treeModels, TreeModel parentTreeModel) {
        List<TreeModel> childrenModel = new ArrayList<>();
        List<TreeModel> tempTreeModels = new LinkedList<>();
        tempTreeModels.addAll(treeModels);
        for (TreeModel treeModel : treeModels) {
            if (treeModel.getPid().equals(parentTreeModel.getId())) {
                childrenModel.add(treeModel);
                tempTreeModels.remove(treeModel);
                if (childrenModel.size() > 0) {
                    recursionTree(tempTreeModels, treeModel);
                }
            }
            parentTreeModel.setChildren(childrenModel);
        }
    }


    //清理序列化后对象所产生的默认值（通过json串进行校验）
    public static void clearObjectDefaultValueByJson(Object obj, JsonObject data) {
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (null != field.get(obj)) {
                    if (!data.has(field.getName())) {
                        field.set(obj, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertDataToEruptView(EruptModel eruptModel, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (null != value) {
                EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(entry.getKey());
                if (null == fieldModel) {
//                    map.put(key, null);
                } else {
                    Edit edit = fieldModel.getEruptField().edit();
                    switch (edit.type()) {
                        case CHOICE:
                            if (edit.choiceType().type() == ChoiceEnum.SELECT_SINGLE || edit.choiceType().type() == ChoiceEnum.RADIO) {
                                map.put(fieldModel.getFieldName(), fieldModel.getChoiceMap().get(value.toString()));
                            }
                            break;
                        case BOOLEAN:
                            map.put(fieldModel.getFieldName(), (Boolean) value ? edit.boolType().trueText() : edit.boolType().falseText());
                            break;
                        case REFERENCE_TREE:
                        case REFERENCE_TABLE:
                        case COMBINE:
//                            convertDataToEruptView(CoreService.ERUPTS.get(fieldModel.getFieldReturnName()), (Map<String, Object>) value);
                            break;
                    }
                }
            }
        }
    }
}
