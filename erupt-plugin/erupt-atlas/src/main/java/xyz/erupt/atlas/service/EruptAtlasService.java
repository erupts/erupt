package xyz.erupt.atlas.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Explore;
import xyz.erupt.annotation.cube.Join;
import xyz.erupt.annotation.cube.Measure;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.atlas.vo.AtlasView;
import xyz.erupt.atlas.vo.FieldRow;
import xyz.erupt.atlas.vo.PowerRow;
import xyz.erupt.atlas.vo.ModelDetail;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;



import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
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
public class EruptAtlasService {

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

    private static final String I18N_PATH = "/i18n/erupt-atlas.i18n.csv";

    // The default in @Power(powerHandler): nothing was plugged in
    private static final String POWER_HANDLER = "PowerHandler";

    // UPMSUtil writes a function button as EruptName@CODE
    private static final char FUN_PERMISSION_SEPARATOR = '@';

    private static List<String> i18nKeys;

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

    public AtlasView build() {
        List<AtlasView.Node> nodes = new ArrayList<>();
        // from|to|kind → label, so ten fields pointing at the same model stay one readable edge
        Map<String, AtlasView.Edge> edges = new LinkedHashMap<>();
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
        List<AtlasView.Edge> edgeList = new ArrayList<>(edges.values());
        return new AtlasView(nodes, edgeList, this.audit(nodes, edgeList), this.pageText());
    }

    /**
     * erupt-cloud: models served by live remote nodes, grouped by the node they live on. The
     * heartbeat only carries the dotted name, and pulling every node's schema would put N HTTP
     * calls on the path of opening the page, so these arrive as nodes without relations.
     */
    private void remoteNodes(List<AtlasView.Node> nodes) {
        EruptRemoteRouter router = EruptRemoteRouterManager.get();
        if (null == router) return;
        for (String remoteName : router.remoteEruptNames()) {
            int dot = remoteName.lastIndexOf(EruptConst.DOT);
            String node = dot > 0 ? remoteName.substring(0, dot) : "remote";
            String simple = dot > 0 ? remoteName.substring(dot + 1) : remoteName;
            nodes.add(new AtlasView.Node(remoteName, simple, simple, node, "remote", false, 0, 0, 0, null));
        }
    }

    private void modelEdges(EruptModel model, Map<String, EruptModel> index, Map<String, AtlasView.Edge> edges) {
        String from = model.getEruptName();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            EruptModel target = index.get(String.valueOf(fieldModel.getFieldReturnName()));
            if (null == target) continue;
            this.addEdge(edges, from, target.getEruptName(), fieldModel.getFieldName(), fieldKind(fieldModel));
        }
        for (Drill drill : model.getErupt().drills()) {
            EruptModel target = index.get(drill.link().linkErupt().getSimpleName());
            if (null != target) {
                this.addEdge(edges, from, target.getEruptName(), i18n(model.getClazz(), drill.title()), "drill");
            }
        }
        for (RowOperation operation : model.getErupt().rowOperation()) {
            if (void.class == operation.eruptClass()) continue;
            EruptModel target = index.get(operation.eruptClass().getSimpleName());
            if (null != target) {
                this.addEdge(edges, from, target.getEruptName(), i18n(model.getClazz(), operation.title()), "operation");
            }
        }
    }

    private void cubeEdges(Class<?> cubeClass, Map<String, EruptModel> index,
                           Map<String, String> tables, Map<String, AtlasView.Edge> edges) {
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

    private void addEdge(Map<String, AtlasView.Edge> edges, String from, String to, String label, String kind) {
        String key = from + "|" + to + "|" + kind;
        AtlasView.Edge exist = edges.get(key);
        if (null == exist) {
            edges.put(key, new AtlasView.Edge(from, to, label, kind));
        } else if (null != label && null != exist.label() && !exist.label().contains(label)) {
            edges.put(key, new AtlasView.Edge(from, to, exist.label() + ", " + label, kind));
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
            fields.add(new ModelDetail.Field(fieldModel.getFieldName(), i18n(model.getClazz(), edit.title()),
                    edit.type().name(),
                    edit.notNull(), edit.search().value(), null == ref ? null : ref.getEruptName()));
        }
        List<String> dataProxy = new ArrayList<>();
        for (Class<? extends DataProxy<?>> proxy : erupt.dataProxy()) dataProxy.add(proxy.getSimpleName());
        return new ModelDetail(model.getEruptName(), i18n(model.getClazz(), erupt.name()),
                i18n(model.getClazz(), erupt.desc()), source(model.getClazz()),
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
                    items.add(new ModelDetail.CubeItem(field.getName(), i18n(cubeClass, dimension.title()), "dimension",
                            dimension.type().name(), dimension.sql()));
                }
                Measure measure = field.getAnnotation(Measure.class);
                if (null != measure) {
                    items.add(new ModelDetail.CubeItem(field.getName(), i18n(cubeClass, measure.title()), "measure",
                            measure.type().name(), measure.sql()));
                }
            }
            return new ModelDetail(simpleName, i18n(cubeClass, cube.name()), i18n(cubeClass, cube.description()),
                    source(cubeClass), null,
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
        if (power.viewDetails()) on.add("detail");
        if (power.export()) on.add("export");
        if (power.importable()) on.add("import");
        if (power.print()) on.add("print");
        if (power.copy()) on.add("copy");
        if (power.cellEdit()) on.add("cellEdit");
        if (power.ai()) on.add("ai");
        return on;
    }

    /* ---------------- audit ---------------- */

    private AtlasView.Audit audit(List<AtlasView.Node> nodes, List<AtlasView.Edge> edges) {
        return new AtlasView.Audit(cycles(nodes, edges), sharedTables(nodes), orphans(nodes, edges), unpublished(nodes));
    }

    /**
     * Tarjan: every strongly connected component bigger than one node is a dependency cycle.
     * A self reference (a tree's parent field) is a single-node component and stays out of the report.
     */
    private static List<List<String>> cycles(List<AtlasView.Node> nodes, Collection<AtlasView.Edge> edges) {
        Map<String, List<String>> adj = new HashMap<>();
        for (AtlasView.Node node : nodes) adj.put(node.id(), new ArrayList<>());
        for (AtlasView.Edge edge : edges) {
            if (STRUCTURAL.contains(edge.kind()) && !edge.from().equals(edge.to())) {
                adj.get(edge.from()).add(edge.to());
            }
        }
        Map<String, Integer> index = new HashMap<>(), low = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        List<List<String>> found = new ArrayList<>();
        int[] counter = {0};
        for (AtlasView.Node node : nodes) {
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
    private static List<AtlasView.SharedTable> sharedTables(List<AtlasView.Node> nodes) {
        Map<String, List<String>> byTable = new TreeMap<>();
        for (AtlasView.Node node : nodes) {
            if (null == node.table()) continue;
            byTable.computeIfAbsent(node.table(), it -> new ArrayList<>()).add(node.name());
        }
        List<AtlasView.SharedTable> shared = new ArrayList<>();
        byTable.forEach((table, models) -> {
            if (models.size() > 1) shared.add(new AtlasView.SharedTable(table, models));
        });
        shared.sort((a, b) -> b.models().size() - a.models().size());
        return shared;
    }

    // Models no relation touches: usually logs and registries, sometimes something forgotten
    private static List<String> orphans(List<AtlasView.Node> nodes, Collection<AtlasView.Edge> edges) {
        Set<String> touched = new HashSet<>();
        for (AtlasView.Edge edge : edges) {
            touched.add(edge.from());
            touched.add(edge.to());
        }
        List<String> list = new ArrayList<>();
        for (AtlasView.Node node : nodes) {
            if ("erupt".equals(node.kind()) && !touched.contains(node.id())) list.add(node.name());
        }
        return list;
    }

    /**
     * Entity-backed models with no menu bound to them. A sub-table or a popup form legitimately
     * has no menu, so this is a candidate list to read, not a defect list to clear.
     */
    private List<String> unpublished(List<AtlasView.Node> nodes) {
        EruptDao eruptDao = eruptDaoProvider.getIfAvailable();
        if (null == eruptDao) return new ArrayList<>();
        Set<String> bound = new LinkedHashSet<>();
        for (EruptMenu menu : eruptDao.lambdaQuery(EruptMenu.class).list()) {
            if (null != menu.getValue()) bound.add(menu.getValue().toLowerCase());
        }
        List<String> list = new ArrayList<>();
        for (AtlasView.Node node : nodes) {
            if (!"erupt".equals(node.kind()) || null == node.table()) continue;
            if (!bound.contains(node.name().toLowerCase())) list.add(node.name());
        }
        return list;
    }

    /**
     * Every field of every model, flattened. Rebuilt per request like everything else here, so a
     * model registered at runtime brings its fields along.
     */
    public List<FieldRow> fields() {
        Map<String, EruptModel> index = new LinkedCaseInsensitiveMap<>();
        for (EruptModel it : EruptCoreService.getErupts()) index.put(it.getEruptName(), it);
        List<FieldRow> rows = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            String label = i18n(model.getClazz(), model.getErupt().name());
            String module = source(model.getClazz());
            for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
                Edit edit = fieldModel.getEruptField().edit();
                EruptModel ref = index.get(String.valueOf(fieldModel.getFieldReturnName()));
                rows.add(new FieldRow(model.getEruptName(), label, module, fieldModel.getFieldName(),
                        i18n(model.getClazz(), edit.title()), edit.type().name(), edit.notNull(),
                        edit.search().value(), null == ref ? null : ref.getEruptName()));
            }
        }
        return rows;
    }

    /**
     * Declared power next to the function buttons the menu tree actually carries. Without
     * erupt-data-jpa there is no menu table to read, and the button side comes back empty.
     */
    public List<PowerRow> power() {
        Map<String, String> menuType = new LinkedCaseInsensitiveMap<>();
        Map<String, Set<String>> buttons = new LinkedCaseInsensitiveMap<>();
        EruptDao eruptDao = eruptDaoProvider.getIfAvailable();
        if (null != eruptDao) {
            for (EruptMenu menu : eruptDao.lambdaQuery(EruptMenu.class).list()) {
                String value = menu.getValue();
                if (null == value) continue;
                int at = value.indexOf(FUN_PERMISSION_SEPARATOR);
                if (at > 0) {
                    buttons.computeIfAbsent(value.substring(0, at), k -> new LinkedHashSet<>())
                            .add(value.substring(at + 1).toUpperCase());
                } else if (!menuType.containsKey(value)) {
                    menuType.put(value, menu.getType());
                }
            }
        }
        List<PowerRow> rows = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            Power power = model.getErupt().power();
            String handler = power.powerHandler().getSimpleName();
            rows.add(new PowerRow(model.getEruptName(), i18n(model.getClazz(), model.getErupt().name()),
                    source(model.getClazz()), menuType.get(model.getEruptName()), powers(power),
                    new ArrayList<>(buttons.getOrDefault(model.getEruptName(), new LinkedHashSet<>())),
                    POWER_HANDLER.equals(handler) ? null : handler));
        }
        return rows;
    }

    /**
     * The page is a static file, so it cannot be rendered in the caller's language: hand it the
     * module's own CSV, translated. Keys are the English text, which is what the page falls back
     * to, so a string missing from the CSV still renders.
     */
    private Map<String, String> pageText() {
        Map<String, String> text = new LinkedHashMap<>();
        for (String key : i18nKeys()) text.put(key, I18nTranslate.$translate(key));
        return text;
    }

    // Read once: the key column of this module's i18n file
    private static synchronized List<String> i18nKeys() {
        if (null != i18nKeys) return i18nKeys;
        List<String> keys = new ArrayList<>();
        try (InputStream is = EruptAtlasService.class.getResourceAsStream(I18N_PATH)) {
            if (null != is) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    boolean header = true;
                    while (null != (line = reader.readLine())) {
                        if (header) {
                            header = false;
                            continue;
                        }
                        String key = csvKey(line);
                        if (!key.isEmpty()) keys.add(key);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(I18N_PATH, e);
        }
        i18nKeys = keys;
        return keys;
    }

    // The first cell, quoted when the English text carries a comma
    private static String csvKey(String line) {
        if (line.startsWith("\"")) {
            int end = line.indexOf('"', 1);
            return end < 0 ? "" : line.substring(1, end);
        }
        int comma = line.indexOf(',');
        return comma < 0 ? line.trim() : line.substring(0, comma).trim();
    }

    /**
     * Annotation text follows the framework rule: a class opts into translation with
     * {@link EruptI18n}, so a user model named the same as a framework key is left alone.
     */
    private static String i18n(Class<?> clazz, String text) {
        if (null == text || text.isEmpty() || null == clazz.getAnnotation(EruptI18n.class)) return text;
        return I18nTranslate.$translate(text);
    }

    private static AtlasView.Node eruptNode(EruptModel model) {
        return new AtlasView.Node(model.getEruptName(), model.getEruptName(),
                i18n(model.getClazz(), model.getErupt().name()),
                source(model.getClazz()), "erupt", EruptCoreService.isRuntimeErupt(model.getEruptName()),
                model.getEruptFieldModels().size(), 0, 0, tableOf(model.getClazz()));
    }

    private static AtlasView.Node cubeNode(Class<?> cubeClass) {
        EruptCube cube = cubeClass.getAnnotation(EruptCube.class);
        int dimensions = 0, measures = 0;
        for (Field field : cubeClass.getDeclaredFields()) {
            if (null != field.getAnnotation(Dimension.class)) dimensions++;
            if (null != field.getAnnotation(Measure.class)) measures++;
        }
        return new AtlasView.Node(CUBE_PREFIX + cubeClass.getSimpleName(), cubeClass.getSimpleName(),
                i18n(cubeClass, cube.name()),
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
