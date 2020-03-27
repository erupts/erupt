package xyz.erupt.core.util;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceEnum;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-04-28.
 */
public class DataHandlerUtil {
    //内存计算的方式生成树结构
    public static List<TreeModel> treeModelToTree(List<TreeModel> treeModels, String rootLabel) {
        List<TreeModel> resultTreeModels = new ArrayList<>();
        List<TreeModel> tempTreeModels = new LinkedList<>();
        tempTreeModels.addAll(treeModels);
        if (StringUtils.isBlank(rootLabel)) {
            for (TreeModel treeModel : treeModels) {
                if (StringUtils.isBlank(treeModel.getPid())) {
                    resultTreeModels.add(treeModel);
                    tempTreeModels.remove(treeModel);
                }
            }
        } else {
            String id = null;
            for (TreeModel treeModel : treeModels) {
                if (rootLabel.equals(treeModel.getLabel())) {
                    id = treeModel.getId();
                    break;
                }
            }
            if (id == null) {
                return resultTreeModels;
            }
            for (TreeModel treeModel : treeModels) {
                if (id.equals(treeModel.getPid())) {
                    resultTreeModels.add(treeModel);
                    tempTreeModels.remove(treeModel);
                }
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
            if (null != treeModel.getPid() && treeModel.getPid().equals(parentTreeModel.getId())) {
                childrenModel.add(treeModel);
                tempTreeModels.remove(treeModel);
                if (childrenModel.size() > 0) {
                    recursionTree(tempTreeModels, treeModel);
                }
                parentTreeModel.setChildren(childrenModel);
            }
        }
    }

    public static void convertDataToEruptView(EruptModel eruptModel, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue()) {
                String key = entry.getKey();
                if (entry.getKey().contains("_")) {
                    key = entry.getKey().split("_")[0];
                }
                EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(key);
                Edit edit = fieldModel.getEruptField().edit();
                switch (edit.type()) {
                    case DATE:
                        if (edit.dateType().type() == DateType.Type.DATE) {
                            if (null != entry.getValue() && StringUtils.isNotBlank(entry.getValue().toString())) {
                                map.put(entry.getKey(), entry.getValue().toString().substring(0, 10));
                            }
                        }
                        break;
                    case DEPEND_SWITCH:
                        map.put(entry.getKey(), fieldModel.getChoiceMap().get(entry.getValue().toString()));
                        break;
                    case CHOICE:
                        if (edit.choiceType().type() == ChoiceEnum.SELECT_SINGLE || edit.choiceType().type() == ChoiceEnum.RADIO) {
                            map.put(entry.getKey(), fieldModel.getChoiceMap().get(entry.getValue().toString()));
                        }
                        break;
                    case BOOLEAN:
                        map.put(entry.getKey(), (Boolean) entry.getValue() ? edit.boolType().trueText() : edit.boolType().falseText());
                        break;
                    case REFERENCE_TREE:
                    case REFERENCE_TABLE:
                    case COMBINE:
                        for (View view : fieldModel.getEruptField().views()) {
                            String[] $keys = entry.getKey().split("_");
                            if (view.column().equals($keys[$keys.length - 1])) {
                                EruptFieldModel vef = CoreService.getErupt(fieldModel.getFieldReturnName()).
                                        getEruptFieldMap().get(view.column());
                                switch (vef.getEruptField().edit().type()) {
                                    case CHOICE:
                                        if (vef.getEruptField().edit().choiceType().type() == ChoiceEnum.SELECT_SINGLE
                                                || vef.getEruptField().edit().choiceType().type() == ChoiceEnum.RADIO) {
                                            map.put(entry.getKey(), vef.getChoiceMap().get(entry.getValue().toString()));
                                        }
                                        break;
                                    case BOOLEAN:
                                        map.put(entry.getKey(), (Boolean) entry.getValue() ?
                                                vef.getEruptField().edit().boolType().trueText() :
                                                vef.getEruptField().edit().boolType().falseText());
                                        break;
                                    case DEPEND_SWITCH:
                                        map.put(entry.getKey(), fieldModel.getChoiceMap().get(entry.getValue().toString()));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        }
    }
}
