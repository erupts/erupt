package xyz.erupt.core.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server side of {@link Tree#maxLevel()}. The UI only hides the "add child" action on nodes at the
 * limit; a form that picks a parent directly, an import or a plain API call can still try to place a
 * node deeper, so every save of a tree entity is checked here as well.
 * <p>
 * The parent chain is read through the same id / pid column query the tree view uses, one query per
 * save, instead of loading parent entities one by one: the result is plain values, so it never runs
 * into lazy proxies, primary key typing or a data layer that has no "find by id" for this model.
 *
 * @author YuePeng
 * date 2026/9/20
 */
@Service
@RequiredArgsConstructor
public class EruptTreeLevelService {

    private final PreEruptDataService preEruptDataService;

    /**
     * Refuse the save when the node, together with the subtree it already carries, would reach past
     * {@code maxLevel}. A node being added has no subtree yet; a node being moved takes its children
     * along, so their height counts too.
     */
    @SneakyThrows
    public void verify(EruptModel eruptModel, Object entity, boolean adding) {
        Tree tree = eruptModel.getErupt().tree();
        if (tree.maxLevel() <= 0 || AnnotationConst.EMPTY_STR.equals(tree.pid())) return;
        Forest forest = this.load(eruptModel, tree);
        String pid = this.text(ReflectUtil.findFieldChain(tree.pid(), entity));
        String id = adding ? null : this.text(ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(entity));
        // the node's own level, plus the levels its existing descendants hang below it
        int level = forest.levelUnder(pid) + forest.below(id);
        if (level > tree.maxLevel()) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("erupt.tree.max_level")
                    .replace("{0}", String.valueOf(tree.maxLevel())), R.PromptWay.MESSAGE);
        }
    }

    private Forest load(EruptModel eruptModel, Tree tree) {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column(tree.id(), AnnotationConst.ID));
        columns.add(new Column(tree.pid(), AnnotationConst.PID));
        Collection<Map<String, Object>> rows = preEruptDataService.createColumnQuery(eruptModel, columns, EruptQuery.builder().build());
        Forest forest = new Forest(ExprInvoke.getExpr(tree.rootPid()));
        for (Map<String, Object> row : rows) {
            forest.add(this.text(row.get(AnnotationConst.ID)), this.text(row.get(AnnotationConst.PID)));
        }
        return forest;
    }

    // Ids compare as text so a Long from the query and an Integer from JSON name the same node
    private String text(Object value) {
        return null == value || StringUtils.isBlank(value.toString()) ? null : value.toString();
    }

    /**
     * The stored tree as id → parent and id → children maps. Bad data can loop, so every walk keeps
     * a visited set; a parent that is missing or filtered away simply ends the count there.
     */
    private static class Forest {

        private final String rootPid;

        private final Map<String, String> parentOf = new HashMap<>();

        private final Map<String, List<String>> childrenOf = new HashMap<>();

        Forest(String rootPid) {
            this.rootPid = StringUtils.isBlank(rootPid) ? null : rootPid;
        }

        void add(String id, String pid) {
            if (null == id) return;
            parentOf.put(id, pid);
            if (!this.isRoot(pid)) childrenOf.computeIfAbsent(pid, it -> new ArrayList<>()).add(id);
        }

        boolean isRoot(String pid) {
            return null == pid || pid.equals(rootPid);
        }

        // Level a node placed under pid would have: 1 for a root, one more than the parent otherwise
        int levelUnder(String pid) {
            int level = 1;
            Set<String> seen = new HashSet<>();
            for (String cur = pid; !this.isRoot(cur) && seen.add(cur); cur = parentOf.get(cur)) level++;
            return level;
        }

        // How many levels of descendants hang below a stored node: 0 for a leaf or a node not stored yet
        int below(String id) {
            return this.below(id, new HashSet<>());
        }

        private int below(String id, Set<String> seen) {
            if (null == id || !seen.add(id)) return 0;
            int deepest = 0;
            for (String child : childrenOf.getOrDefault(id, List.of())) deepest = Math.max(deepest, 1 + this.below(child, seen));
            return deepest;
        }

    }

}
