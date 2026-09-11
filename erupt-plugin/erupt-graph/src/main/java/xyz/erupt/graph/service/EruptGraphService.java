package xyz.erupt.graph.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Explore;
import xyz.erupt.annotation.cube.Join;
import xyz.erupt.annotation.cube.Measure;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.graph.vo.GraphView;
import xyz.erupt.graph.vo.ModelDetail;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the live erupt registry into a graph. Read-only and rebuilt on every request, so a model
 * registered at runtime (erupt-designer, erupt-flow forms) shows up the moment it is published.
 *
 * @author YuePeng
 */
@Service
public class EruptGraphService {

    // Cube ids are namespaced: a class may carry both @Erupt and @EruptCube (see EruptOperateLog)
    private static final String CUBE_PREFIX = "cube#";

    // Physical tables a cube reads, good enough to tie a cube back to the models it aggregates
    private static final Pattern SQL_TABLE = Pattern.compile("(?i)\\b(?:from|join)\\s+([a-zA-Z_][\\w$]*)");

    private static final String JPA_TABLE = "jakarta.persistence.Table";

    private static final String JPA_ENTITY = "jakarta.persistence.Entity";

    // Only real data relations can form a dependency cycle worth breaking: a drill or a row
    // operation pointing back is navigation, not coupling
    private static final Set<String> STRUCTURAL = Set.of("reference", "tab", "embed");

    private static final String PACKAGE_PREFIX = "xyz.erupt.";

    // @EruptCube classes are plain classes, not erupt models, so they are discovered by scan once
    private final List<Class<?>> cubeClasses = new ArrayList<>();

    // Absent when the app runs without erupt-data-jpa; the menu audit is simply skipped then
    @Resource
    private ObjectProvider<EruptDao> eruptDaoProvider;

    public void scanCubes() {
        cubeClasses.clear();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(),
                new TypeFilter[]{new AnnotationTypeFilter(EruptCube.class)}, cubeClasses::add);
    }

    public GraphView build() {
        List<GraphView.Node> nodes = new ArrayList<>();
        // from|to|kind → label, so ten fields pointing at the same model stay one readable edge
        Map<String, GraphView.Edge> edges = new LinkedHashMap<>();
        Map<String, EruptModel> index = new LinkedCaseInsensitiveMap<>();
        Map<String, String> tables = new LinkedCaseInsensitiveMap<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            index.put(model.getEruptName(), model);
            String table = tableOf(model.getClazz());
            if (null != table) tables.put(table, model.getEruptName());
        }
        for (EruptModel model : EruptCoreService.getErupts()) {
            nodes.add(eruptNode(model));
            this.modelEdges(model, index, edges);
        }
        for (Class<?> cubeClass : cubeClasses) {
            nodes.add(cubeNode(cubeClass));
            this.cubeEdges(cubeClass, index, tables, edges);
        }
        this.remoteNodes(nodes);
        List<GraphView.Edge> edgeList = new ArrayList<>(edges.values());
        return new GraphView(nodes, edgeList, this.audit(nodes, edgeList));
    }

    /**
     * erupt-cloud: models served by live remote nodes, grouped by the node they live on. The
     * heartbeat only carries the dotted name, and pulling every node's schema would put N HTTP
     * calls on the path of opening the page, so these arrive as nodes without relations.
     */
    private void remoteNodes(List<GraphView.Node> nodes) {
        EruptRemoteRouter router = EruptRemoteRouterManager.get();
        if (null == router) return;
        for (String remoteName : router.remoteEruptNames()) {
            int dot = remoteName.lastIndexOf(EruptConst.DOT);
            String node = dot > 0 ? remoteName.substring(0, dot) : "remote";
            String simple = dot > 0 ? remoteName.substring(dot + 1) : remoteName;
            nodes.add(new GraphView.Node(remoteName, simple, simple, node, "remote", false, 0, 0, 0, null));
        }
    }

    private void modelEdges(EruptModel model, Map<String, EruptModel> index, Map<String, GraphView.Edge> edges) {
        String from = model.getEruptName();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            EruptModel target = index.get(String.valueOf(fieldModel.getFieldReturnName()));
            if (null == target) continue;
            this.addEdge(edges, from, target.getEruptName(), fieldModel.getFieldName(), fieldKind(fieldModel));
        }
        for (Drill drill : model.getErupt().drills()) {
            EruptModel target = index.get(drill.link().linkErupt().getSimpleName());
            if (null != target) this.addEdge(edges, from, target.getEruptName(), drill.title(), "drill");
        }
        for (RowOperation operation : model.getErupt().rowOperation()) {
            if (void.class == operation.eruptClass()) continue;
            EruptModel target = index.get(operation.eruptClass().getSimpleName());
            if (null != target) this.addEdge(edges, from, target.getEruptName(), operation.title(), "operation");
        }
    }

    private void cubeEdges(Class<?> cubeClass, Map<String, EruptModel> index,
                           Map<String, String> tables, Map<String, GraphView.Edge> edges) {
        String from = CUBE_PREFIX + cubeClass.getSimpleName();
        EruptCube cube = cubeClass.getAnnotation(EruptCube.class);
        // a class carrying both annotations analyses the very model it is declared on
        EruptModel owner = index.get(cubeClass.getSimpleName());
        if (null != owner) this.addEdge(edges, from, owner.getEruptName(), null, "cubeOf");
        Matcher matcher = SQL_TABLE.matcher(cube.sql());
        while (matcher.find()) {
            String eruptName = tables.get(matcher.group(1));
            if (null != eruptName && (null == owner || !eruptName.equals(owner.getEruptName()))) {
                this.addEdge(edges, from, eruptName, matcher.group(1), "table");
            }
        }
        for (Explore explore : cube.explores()) {
            for (Join join : explore.joins()) {
                if (null != join.cube().getAnnotation(EruptCube.class)) {
                    this.addEdge(edges, from, CUBE_PREFIX + join.cube().getSimpleName(), explore.name(), "join");
                }
            }
        }
    }

    private void addEdge(Map<String, GraphView.Edge> edges, String from, String to, String label, String kind) {
        String key = from + "|" + to + "|" + kind;
        GraphView.Edge exist = edges.get(key);
        if (null == exist) {
            edges.put(key, new GraphView.Edge(from, to, label, kind));
        } else if (null != label && null != exist.label() && !exist.label().contains(label)) {
            edges.put(key, new GraphView.Edge(from, to, exist.label() + ", " + label, kind));
        }
    }

    /* ---------------- one model ---------------- */

    public ModelDetail detail(String id) {
        if (id.startsWith(CUBE_PREFIX)) return this.cubeDetail(id.substring(CUBE_PREFIX.length()));
        // Read from the registry list, not getErupt(): under hot-build that rebuilds the model and
        // hands back one whose field json was never rendered
        EruptModel model = null;
        for (EruptModel it : EruptCoreService.getErupts()) {
            if (it.getEruptName().equalsIgnoreCase(id)) {
                model = it;
                break;
            }
        }
        if (null == model) return null;
        Erupt erupt = model.getErupt();
        Map<String, EruptModel> index = new LinkedCaseInsensitiveMap<>();
        for (EruptModel it : EruptCoreService.getErupts()) index.put(it.getEruptName(), it);
        List<ModelDetail.Field> fields = new ArrayList<>();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            // The proxied annotation already resolved AUTO into the component actually rendered
            Edit edit = fieldModel.getEruptField().edit();
            EruptModel ref = index.get(String.valueOf(fieldModel.getFieldReturnName()));
            fields.add(new ModelDetail.Field(fieldModel.getFieldName(), edit.title(), edit.type().name(),
                    edit.notNull(), edit.search().value(), null == ref ? null : ref.getEruptName()));
        }
        List<String> dataProxy = new ArrayList<>();
        for (Class<? extends DataProxy<?>> proxy : erupt.dataProxy()) dataProxy.add(proxy.getSimpleName());
        return new ModelDetail(model.getEruptName(), erupt.name(), erupt.desc(), source(model.getClazz()),
                tableOf(model.getClazz()), "erupt", EruptCoreService.isRuntimeErupt(model.getEruptName()),
                erupt.primaryKeyCol(), powers(erupt.power()), dataProxy, fields, new ArrayList<>());
    }

    private ModelDetail cubeDetail(String simpleName) {
        for (Class<?> cubeClass : cubeClasses) {
            if (!cubeClass.getSimpleName().equals(simpleName)) continue;
            EruptCube cube = cubeClass.getAnnotation(EruptCube.class);
            List<ModelDetail.CubeItem> items = new ArrayList<>();
            for (Field field : cubeClass.getDeclaredFields()) {
                Dimension dimension = field.getAnnotation(Dimension.class);
                if (null != dimension) {
                    items.add(new ModelDetail.CubeItem(field.getName(), dimension.title(), "dimension",
                            dimension.type().name(), dimension.sql()));
                }
                Measure measure = field.getAnnotation(Measure.class);
                if (null != measure) {
                    items.add(new ModelDetail.CubeItem(field.getName(), measure.title(), "measure",
                            measure.type().name(), measure.sql()));
                }
            }
            return new ModelDetail(simpleName, cube.name(), cube.description(), source(cubeClass), null,
                    "cube", false, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), items);
        }
        return null;
    }

    private static List<String> powers(Power power) {
        List<String> on = new ArrayList<>();
        if (power.add()) on.add("add");
        if (power.edit()) on.add("edit");
        if (power.delete()) on.add("delete");
        if (power.query()) on.add("query");
        if (power.export()) on.add("export");
        if (power.importable()) on.add("import");
        if (power.print()) on.add("print");
        if (power.copy()) on.add("copy");
        return on;
    }

    /* ---------------- audit ---------------- */

    private GraphView.Audit audit(List<GraphView.Node> nodes, List<GraphView.Edge> edges) {
        return new GraphView.Audit(cycles(nodes, edges), sharedTables(nodes), orphans(nodes, edges), unpublished(nodes));
    }

    /**
     * Tarjan: every strongly connected component bigger than one node is a dependency cycle.
     * A self reference (a tree's parent field) is a single-node component and stays out of the report.
     */
    private static List<List<String>> cycles(List<GraphView.Node> nodes, Collection<GraphView.Edge> edges) {
        Map<String, List<String>> adj = new HashMap<>();
        for (GraphView.Node node : nodes) adj.put(node.id(), new ArrayList<>());
        for (GraphView.Edge edge : edges) {
            if (STRUCTURAL.contains(edge.kind()) && !edge.from().equals(edge.to())) {
                adj.get(edge.from()).add(edge.to());
            }
        }
        Map<String, Integer> index = new HashMap<>(), low = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        List<List<String>> found = new ArrayList<>();
        int[] counter = {0};
        for (GraphView.Node node : nodes) {
            if (!index.containsKey(node.id())) {
                strongConnect(node.id(), adj, index, low, stack, onStack, counter, found);
            }
        }
        found.sort((a, b) -> b.size() - a.size());
        return found;
    }

    private static void strongConnect(String id, Map<String, List<String>> adj, Map<String, Integer> index,
                                      Map<String, Integer> low, Deque<String> stack, Set<String> onStack,
                                      int[] counter, List<List<String>> found) {
        index.put(id, counter[0]);
        low.put(id, counter[0]);
        counter[0]++;
        stack.push(id);
        onStack.add(id);
        for (String next : adj.getOrDefault(id, new ArrayList<>())) {
            if (!index.containsKey(next)) {
                strongConnect(next, adj, index, low, stack, onStack, counter, found);
                low.put(id, Math.min(low.get(id), low.get(next)));
            } else if (onStack.contains(next)) {
                low.put(id, Math.min(low.get(id), index.get(next)));
            }
        }
        if (low.get(id).equals(index.get(id))) {
            List<String> component = new ArrayList<>();
            String member;
            do {
                member = stack.pop();
                onStack.remove(member);
                component.add(member);
            } while (!member.equals(id));
            if (component.size() > 1) found.add(component);
        }
    }

    // Several @Erupt classes mapped onto one physical table: erupt's own narrower-projection idiom
    // (EruptUser and its three view models), and the place a careless schema change bites twice
    private static List<GraphView.SharedTable> sharedTables(List<GraphView.Node> nodes) {
        Map<String, List<String>> byTable = new TreeMap<>();
        for (GraphView.Node node : nodes) {
            if (null == node.table()) continue;
            byTable.computeIfAbsent(node.table(), it -> new ArrayList<>()).add(node.name());
        }
        List<GraphView.SharedTable> shared = new ArrayList<>();
        byTable.forEach((table, models) -> {
            if (models.size() > 1) shared.add(new GraphView.SharedTable(table, models));
        });
        shared.sort((a, b) -> b.models().size() - a.models().size());
        return shared;
    }

    // Models no relation touches: usually logs and registries, sometimes something forgotten
    private static List<String> orphans(List<GraphView.Node> nodes, Collection<GraphView.Edge> edges) {
        Set<String> touched = new HashSet<>();
        for (GraphView.Edge edge : edges) {
            touched.add(edge.from());
            touched.add(edge.to());
        }
        List<String> list = new ArrayList<>();
        for (GraphView.Node node : nodes) {
            if ("erupt".equals(node.kind()) && !touched.contains(node.id())) list.add(node.name());
        }
        return list;
    }

    /**
     * Entity-backed models with no menu bound to them. A sub-table or a popup form legitimately
     * has no menu, so this is a candidate list to read, not a defect list to clear.
     */
    private List<String> unpublished(List<GraphView.Node> nodes) {
        EruptDao eruptDao = eruptDaoProvider.getIfAvailable();
        if (null == eruptDao) return new ArrayList<>();
        Set<String> bound = new LinkedHashSet<>();
        for (EruptMenu menu : eruptDao.lambdaQuery(EruptMenu.class).list()) {
            if (null != menu.getValue()) bound.add(menu.getValue().toLowerCase());
        }
        List<String> list = new ArrayList<>();
        for (GraphView.Node node : nodes) {
            if (!"erupt".equals(node.kind()) || null == node.table()) continue;
            if (!bound.contains(node.name().toLowerCase())) list.add(node.name());
        }
        return list;
    }

    private static GraphView.Node eruptNode(EruptModel model) {
        return new GraphView.Node(model.getEruptName(), model.getEruptName(), model.getErupt().name(),
                source(model.getClazz()), "erupt", EruptCoreService.isRuntimeErupt(model.getEruptName()),
                model.getEruptFieldModels().size(), 0, 0, tableOf(model.getClazz()));
    }

    private static GraphView.Node cubeNode(Class<?> cubeClass) {
        EruptCube cube = cubeClass.getAnnotation(EruptCube.class);
        int dimensions = 0, measures = 0;
        for (Field field : cubeClass.getDeclaredFields()) {
            if (null != field.getAnnotation(Dimension.class)) dimensions++;
            if (null != field.getAnnotation(Measure.class)) measures++;
        }
        return new GraphView.Node(CUBE_PREFIX + cubeClass.getSimpleName(), cubeClass.getSimpleName(), cube.name(),
                source(cubeClass), "cube", false, 0, dimensions, measures, null);
    }

    private static String fieldKind(EruptFieldModel fieldModel) {
        return switch (fieldModel.getEruptField().edit().type()) {
            case TAB_TREE, TAB_TABLE_ADD, TAB_TABLE_REFER, CHECKBOX -> "tab";
            case COMBINE, MULTI_FORM -> "embed";
            default -> "reference";
        };
    }

    // Module the model was declared in, read off the package: xyz.erupt.upms.model.X → upms
    private static String source(Class<?> clazz) {
        if (null == clazz || null == clazz.getPackage()) return null;
        String pack = clazz.getPackage().getName();
        if (!pack.startsWith(PACKAGE_PREFIX)) return pack;
        String tail = pack.substring(PACKAGE_PREFIX.length());
        int dot = tail.indexOf('.');
        return dot > 0 ? tail.substring(0, dot) : tail;
    }

    // Read @Table(name) reflectively: the JPA dependency here is provided-scope and may be absent
    private static String tableOf(Class<?> clazz) {
        if (null == clazz) return null;
        boolean entity = false;
        for (Annotation annotation : clazz.getAnnotations()) {
            if (JPA_ENTITY.equals(annotation.annotationType().getName())) entity = true;
        }
        if (!entity) return null;
        for (Annotation annotation : clazz.getAnnotations()) {
            if (!JPA_TABLE.equals(annotation.annotationType().getName())) continue;
            try {
                String name = (String) annotation.annotationType().getMethod("name").invoke(annotation);
                return name.isEmpty() ? null : name;
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }
        return null;
    }

}
