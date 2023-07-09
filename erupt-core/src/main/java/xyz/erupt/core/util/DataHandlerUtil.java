package xyz.erupt.core.util;

import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

import java.util.*;

/**
 * @author YuePeng
 * date 2019-04-28.
 */
public class DataHandlerUtil {

    // 引用方式 生成树结构数据
    public static List<TreeModel> quoteTree(List<TreeModel> treeModels) {
        Map<String, TreeModel> treeModelMap = new LinkedHashMap<>(treeModels.size());
        treeModels.forEach(treeModel -> treeModelMap.put(treeModel.getId(), treeModel));
        List<TreeModel> resultTreeModels = new ArrayList<>();
        treeModels.forEach(treeModel -> {
            if (treeModel.isRoot()) {
                treeModel.setLevel(1);
                resultTreeModels.add(treeModel);
                return;
            }
            Optional.ofNullable(treeModelMap.get(treeModel.getPid())).ifPresent(parentTreeModel -> {
                Collection<TreeModel> children = CollectionUtils.isEmpty(parentTreeModel.getChildren()) ? new ArrayList<>() : parentTreeModel.getChildren();
                children.add(treeModel);
                children.forEach(child -> child.setLevel(Optional.ofNullable(parentTreeModel.getLevel()).orElse(1) + 1));
                parentTreeModel.setChildren(children);
            });
        });
        return resultTreeModels;
    }

    private static EruptFieldModel cycleFindFieldByKey(EruptModel eruptModel, String key) {
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(key);
        if (null != fieldModel) {
            return fieldModel;
        }
        if (key.contains("_")) {
            return cycleFindFieldByKey(eruptModel, key.substring(0, key.lastIndexOf("_")));
        }
        return null;
    }

    public static void convertDataToEruptView(EruptModel eruptModel, Collection<Map<String, Object>> list) {
        Map<String, Map<String, String>> choiceItems = new HashMap<>();
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                EruptFieldModel fieldModel = cycleFindFieldByKey(eruptModel, entry.getKey());
                if (null == fieldModel) {
                    continue;
                }
                Edit edit = fieldModel.getEruptField().edit();
                switch (edit.type()) {
                    case REFERENCE_TREE:
                    case REFERENCE_TABLE:
                    case COMBINE:
                        String[] _keys = entry.getKey().split("_");
                        for (View view : fieldModel.getEruptField().views()) {
                            if (view.column().equals(_keys[_keys.length - 1])) {
                                EruptFieldModel vef = EruptCoreService.getErupt(fieldModel.getFieldReturnName()).getEruptFieldMap().get(view.column());
                                map.put(entry.getKey(), convertColumnValue(eruptModel, vef, entry.getValue(), choiceItems));
                            }
                        }
                        break;
                    default:
                        map.put(entry.getKey(), convertColumnValue(eruptModel, fieldModel, entry.getValue(), choiceItems));
                        break;
                }
            }
        }
    }

    private static Object convertColumnValue(EruptModel eruptModel, EruptFieldModel fieldModel, Object value, Map<String, Map<String, String>> choiceItems) {
        if (null == value) return null;
        Edit edit = fieldModel.getEruptField().edit();
        switch (edit.type()) {
            case CHOICE:
                Map<String, String> cm = choiceItems.get(fieldModel.getFieldName());
                if (null == cm) {
                    cm = EruptUtil.getChoiceMap(eruptModel, edit.choiceType());
                    choiceItems.put(fieldModel.getFieldName(), cm);
                }
                return cm.get(value.toString());
            case BOOLEAN:
                return (Boolean) value ? edit.boolType().trueText() : edit.boolType().falseText();
        }
        return value;
    }


}