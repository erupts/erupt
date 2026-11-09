package xyz.erupt.core.util;

import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

import java.util.*;

/**
 * @author YuePeng
 * date 2019-04-28.
 */
public class DataHandlerUtil {

    // Reference method: Generate tree structure data
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

    /**
     * Prepare queried rows for the client. Values stay raw — the wording of a BOOLEAN or the label
     * of a CHOICE belongs to whoever displays them, and a client that has to edit, filter or sort
     * by a value needs the value itself. Only PASSWORD is rewritten, because its clear text must
     * never leave the server.
     */
    public static void convertDataToEruptView(EruptModel eruptModel, Collection<Map<String, Object>> list) {
        Set<String> passwords = passwordKeys(eruptModel);
        if (passwords.isEmpty()) return;
        for (Map<String, Object> map : list) {
            for (String key : passwords) {
                if (null != map.get(key)) map.put(key, EruptConst.PASSWORD_PLACEHOLDER);
            }
        }
    }

    /**
     * The row keys a PASSWORD view is read from, taken from the model rather than guessed from a key: the field itself,
     * or field_column for a column of the referenced erupt, as the query aliases it. Splitting a key at its last "_"
     * found the wrong field for snake_case names and masked every view of the field it found.
     */
    static Set<String> passwordKeys(EruptModel eruptModel) {
        Set<String> keys = new HashSet<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            for (View view : fieldModel.getEruptField().views()) {
                if (ViewType.PASSWORD != view.type()) continue;
                keys.add(view.column().isEmpty() ? fieldModel.getFieldName()
                        : fieldModel.getFieldName() + "_" + view.column().replace(EruptConst.DOT, "_"));
            }
        }
        return keys;
    }

}
