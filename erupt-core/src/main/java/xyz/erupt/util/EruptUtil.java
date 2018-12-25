package xyz.erupt.util;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.base.model.TreeModel;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

    //数据项与erupt注解中描述不相符时使用
    public static final String NOT_ERUPT_REF = "@NOT_REF@";

    //FIXME 通过hibernate的OneToMany加载数据的特性生成树（执行sql的量比较大，影响性能！！！！）
    public static void generateTree(Collection collection, Tree tree) {
        try {
            for (Object o : collection) {
                for (Field field : o.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getName().equals(tree.children())) {
                        Collection fc = (Collection) field.get(o);
                        if (null != fc && fc.size() > 0) {
                            generateTree((Collection) field.get(o), tree);
                        }
                    } else {
                        if (!field.getName().equals(tree.id()) && !field.getName().equals(tree.label())) {

                            field.set(o, null);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 内存计算的方式生成树结构
     *
     * @param treeModels
     * @return
     */
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


    public Map<String, Object> EruptDataToViewData(Object data) {
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return result;
    }

}
