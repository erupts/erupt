package com.erupt.util;

import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.model.core.TreeModel;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

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
     * @param treeModels
     * @param resultTreeModels
     * @return
     */
    public static List<TreeModel> TreeModelToTree(List<TreeModel> treeModels, List<TreeModel> resultTreeModels) {
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

}
