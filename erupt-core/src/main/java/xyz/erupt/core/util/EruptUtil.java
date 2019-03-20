package xyz.erupt.core.util;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.TreeModel;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

    //数据项与erupt注解中描述不相符时使用
    private static final String NOT_ERUPT_REF = "@NOT_REF@";

    //内存计算的方式生成树结构
    public static List<TreeModel> TreeModelToTree(List<TreeModel> treeModels) {
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
        return resultTreeModels;
    }

    private static void recursionTree(List<TreeModel> treeModels, TreeModel ParentTreeModel) {
        List<TreeModel> childrenModel = new ArrayList<>();
        List<TreeModel> tempTreeModels = new LinkedList<>();
        tempTreeModels.addAll(treeModels);
        for (TreeModel treeModel : treeModels) {
            if (treeModel.getPid().equals(ParentTreeModel.getId())) {
                childrenModel.add(treeModel);
                tempTreeModels.remove(treeModel);
                if (childrenModel.size() > 0) {
                    recursionTree(tempTreeModels, treeModel);
                }
            }
            ParentTreeModel.setChildren(childrenModel);
        }
    }

    public static String handleNoRightVariable(String pathVariable) {
        if (pathVariable.startsWith(RestPath.NO_RIGHT_SYMBOL)) {
            return pathVariable.substring(2, pathVariable.length());
        } else {
            throw new RuntimeException("数据参数异常");
        }
    }


    public static void rinseEruptObj(Object eruptObj) throws IllegalAccessException {
        for (Field field : eruptObj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            EruptField eruptField = field.getAnnotation(EruptField.class);
            if (null != eruptField) {
                if (eruptField.edit().type() == EditType.TAB) {
                    field.set(eruptObj, null);
                }
            } else {
                field.set(eruptObj, null);
            }
        }
    }


    public Map<String, Object> eruptDataToViewData(Object data) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (Field field : data.getClass().getDeclaredFields()) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                Object fieldData = field.get(data);
                if (null != eruptField) {
                    switch (eruptField.edit().type()) {
                        case INPUT:
                            result.put(field.getName(), fieldData);
                            break;
                        case CHOICE:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                for (VL vl : eruptField.edit().choiceType()[0].vl()) {
                                    if ((vl.value() + "").equals(fieldData.toString())) {
                                        result.put(field.getName(), fieldData);
                                        break;
                                    }
                                }
                                //如果与VL注解无匹配项则注入该标识信息
                                if (StringUtils.isBlank(result.get(field.getName()).toString())) {
                                    result.put(field.getName(), NOT_ERUPT_REF);
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        case BOOLEAN:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                Boolean boolField = new Boolean(fieldData.toString());
                                if (boolField) {
                                    result.put(field.getName(), eruptField.edit().boolType()[0].trueText());
                                } else {
                                    result.put(field.getName(), eruptField.edit().boolType()[0].trueText());
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        case REFERENCE:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                for (View view : eruptField.views()) {
                                    result.put(field.getName() + "_" + view.column(),
                                            fieldData.getClass().getDeclaredField(view.column()).get(fieldData));
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        default:
                            result.put(field.getName(), field.get(data));
                            break;
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return result;
    }

}
