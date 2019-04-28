package xyz.erupt.core.util;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.model.TreeModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
}
