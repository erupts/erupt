package xyz.erupt.cloud.node.lite;

import com.sun.net.httpserver.HttpServer;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.fun.AutoCompleteHandler;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.common.model.NodeInfo;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.cloud.common.node.NodeCapability;
import xyz.erupt.cloud.common.node.NodeRegistrar;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An erupt cloud node speaking protocol v2 (erupt-cloud/PROTOCOL.md §12): registers with the server, then answers
 * the signed, checked and normalized requests it forwards (Level 0, row operations, fetched options and buttons through
 * {@code invoke}). The server enforces rights, checks queries and records and reads reference pickers itself. Serve it with the JDK's HTTP server ({@link #serve}), or mount
 * {@link #handle} in any other HTTP stack and call {@link #connect}: erupt-cloud-node-solon, -quarkus and -vertx do
 * that for their frameworks.
 *
 * <pre>{@code
 * new Node("blog", System.getenv("ERUPT_ACCESS_TOKEN"), "http://erupt-server:8080")
 *     .register(Article.class, new JdbcRepository<>(dataSource, "article"))
 *     .serve("0.0.0.0", 9090);
 * }</pre>
 */
public final class Node implements AutoCloseable, Model.Registry {

    /** The erupt version this node is built with. */
    public static final String VERSION = version();

    /** The prefix of every model endpoint the server forwards to a node. */
    public static final String ERUPT_API = "/erupt-api";

    // request headers the server forwards (EruptMutualConst in erupt-core)
    private static final String HEADER_ERUPT = "erupt", HEADER_ERUPT_PARENT = "eruptParent", HEADER_USER = "user", HEADER_TOKEN = "token";

    // the query param naming the parent erupt, when not sent as the eruptParent header
    private static final String PARAM_ERUPT_PARENT = "_eruptParent";

    // what Erupt-Enforced must list for an endpoint (§12.2)
    private static final Set<CloudSignature.Enforced> POWER = EnumSet.of(CloudSignature.Enforced.POWER),
            QUERY = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.QUERY),
            RECORD = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD);

    private static final System.Logger LOG = System.getLogger("erupt");

    /**
     * Features whose endpoints this node does not serve yet: hidden from the UI instead of failing there. Export,
     * import and cell editing pass: the server does them for every node through the endpoints a node serves.
     */
    private static final Set<Right> UNSUPPORTED_POWER = EnumSet.of(Right.PRINT, Right.AI);

    private static final int MAX_PAGE_SIZE = 1000, MAX_AGGREGATE_ROWS = 10_000;

    private static final ThreadLocal<Scope> SCOPE = new ThreadLocal<>();

    private final String name, accessToken;
    private final NodeRegistrar registrar;
    private final Map<String, Entry<?>> entries = new LinkedHashMap<>();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Map<Class<?>, Model<?>> models = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final List<Route> routes = new ArrayList<>();
    private final CountDownLatch stopped = new CountDownLatch(1);
    private String address;
    private Duration heartbeat = Duration.ofSeconds(15);
    private Function<Class<?>, Object> factory = type -> null;
    private HttpServer server;
    private ExecutorService executor;
    private volatile boolean closed;

    /** {@code servers}: one or more erupt server base URLs, round-robined. */
    public Node(String name, String accessToken, String... servers) {
        if (accessToken == null || accessToken.isEmpty()) throw new IllegalArgumentException("accessToken is required");
        if (servers.length == 0) throw new IllegalArgumentException("at least one server address is required");
        this.name = name;
        this.accessToken = accessToken;
        this.registrar = new NodeRegistrar(name, accessToken, List.of(servers));
        // the server reads builds on its own account: no Erupt-Enforced needed
        route("GET", ERUPT_API + "/build/([^/]+)", Set.of(), this::build);
        route("POST", ERUPT_API + "/data/table/([^/]+)", QUERY, this::table);
        route("POST", ERUPT_API + "/data/aggregate/([^/]+)", QUERY, this::aggregate);
        route("POST", ERUPT_API + "/invoke/([^/]+)/([^/]+)/([^/]+)", POWER, this::invoke);
        route("GET", ERUPT_API + "/data/init-value/([^/]+)", POWER, this::initValue);
        route("GET", ERUPT_API + "/data/([^/]+)/([^/]+)", POWER, this::get);
        route("POST", ERUPT_API + "/data/modify/([^/]+)", RECORD, this::add);
        route("POST", ERUPT_API + "/data/modify/([^/]+)/update", RECORD, this::update);
        route("POST", ERUPT_API + "/data/modify/([^/]+)/delete", POWER, this::delete);
    }

    // ---------------------------------------------------------------- configuration

    /** The base URL the server calls this node on; detected from the route to the first server when unset. */
    public Node address(String address) {
        this.address = address.replaceAll("/+$", "");
        return this;
    }

    /** The base URL set by {@link #address(String)} or detected by {@link #connect(int)}; null before either. */
    public String address() {
        return address;
    }

    public Node heartbeat(Duration heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    /**
     * Supplies handler instances ({@code DataProxy}, {@code OperationHandler}, {@code ChoiceFetchHandler},
     * {@code PowerHandler}, {@code ExprHandler}...): hand it your DI container's lookup. Each class is asked once; a
     * null answer falls back to the no-argument constructor.
     */
    public Node factory(Function<Class<?>, Object> factory) {
        this.factory = factory;
        return this;
    }

    /** Serves an {@code @Erupt} class from {@code repository}. Referenced models must be registered too. */
    public <T> Node register(Class<T> type, Repository<T> repository) {
        Model<T> model = model0(type);
        if (entries.containsKey(model.name)) throw new IllegalArgumentException("erupt '" + model.name + "' registered twice");
        if (model.pk() == null) throw new IllegalArgumentException(model.name + " has no primary key field '" + model.erupt.primaryKeyCol() + "'");
        repository.bind(model);
        entries.put(model.name, new Entry<>(model, repository));
        return this;
    }

    /** The repository a class is registered with. */
    @SuppressWarnings("unchecked")
    public <T> Repository<T> repository(Class<T> type) {
        Entry<?> e = entries.get(type.getSimpleName());
        if (e == null || e.model.type != type) throw new IllegalArgumentException(type.getSimpleName() + " is not registered on this node");
        return (Repository<T>) e.repo;
    }

    /** The node serving the current request, for handlers that need a repository. */
    public static Node current() {
        Scope s = SCOPE.get();
        return s == null ? null : s.node;
    }

    /**
     * The acting user's full information from the server (account, name, roles, org...), for code inside a request;
     * PROTOCOL.md §6. Fails outside a request or when the server cannot be reached.
     */
    public static Map<?, ?> userInfo() throws IOException, InterruptedException {
        Scope s = scope();
        return Json.parse(s.node.serverGet(CloudRestApiConst.ERUPT_USER_INFO + "/" + s.node.name, s.token)) instanceof Map<?, ?> m ? m : Map.of();
    }

    /** The node's config text, set on its node record on the server (empty when none); PROTOCOL.md §6. */
    public static String config() throws IOException, InterruptedException {
        Scope s = scope();
        return s.node.serverGet(CloudRestApiConst.NODE_CONFIG + "/" + s.node.name, null);
    }

    private static Scope scope() {
        Scope s = SCOPE.get();
        if (s == null) throw new IllegalStateException("not inside a request of an erupt node");
        return s;
    }

    // a call back into the server, authenticated by the access token (and the user's session token, when given)
    private String serverGet(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(registrar.nextServer() + path)).timeout(Duration.ofSeconds(5))
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, accessToken).GET();
        if (token != null) req.header(HEADER_TOKEN, token);
        HttpResponse<String> resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new IOException("HTTP " + resp.statusCode() + " from " + path);
        return resp.body();
    }

    /** The acting user of the current request; null outside one or when the server sent none. */
    public static User currentUser() {
        Scope s = SCOPE.get();
        return s == null ? null : s.user;
    }

    @Override
    public Object instance(Class<?> type) {
        return instances.computeIfAbsent(type, t -> {
            Object instance = factory.apply(t);
            return instance == null ? Model.Registry.construct(t) : instance;
        });
    }

    @Override
    public Model<?> model(Class<?> type) {
        return model0(type);
    }

    @SuppressWarnings("unchecked")
    private <T> Model<T> model0(Class<T> type) {
        Model<?> m = models.get(type);
        if (m == null) models.put(type, m = new Model<>(type, this));
        return (Model<T>) m;
    }

    // ---------------------------------------------------------------- framework-neutral handler

    /**
     * One request. {@code target} is the raw path and query below the node's mount point, exactly as received
     * (the signature covers it); {@code header} looks a header up case-insensitively (null when absent).
     */
    public record Request(String method, String target, Function<String, String> header, byte[] body) {

        /** The target of a raw path and query received under {@code mount}: a context path or mount point, null for none. */
        public static String target(String mount, String rawPath, String rawQuery) {
            String prefix = mount == null ? "" : mount.replaceAll("/+$", "");
            String path = !prefix.isEmpty() && rawPath.startsWith(prefix) ? rawPath.substring(prefix.length()) : rawPath;
            return path + (rawQuery == null || rawQuery.isEmpty() ? "" : "?" + rawQuery);
        }
    }

    public record Response(int status, String contentType, byte[] body) {
    }

    /** Answers one request; never throws. Mount it in any HTTP stack (servlet, Ktor, Vert.x, Javalin...). */
    public Response handle(Request req) {
        int q = req.target().indexOf('?');
        String path = q < 0 ? req.target() : req.target().substring(0, q);
        Object body;
        int status = 200;
        try {
            body = dispatch(req, path, q < 0 ? "" : req.target().substring(q + 1));
        } catch (Reply r) {
            status = r.status;
            body = "";
        } catch (EruptException e) {
            body = r(false, e.getMessage(), null);
        } catch (Exception e) { // never leak internals: message only, the stack stays in the node log
            LOG.log(System.Logger.Level.ERROR, "erupt node error", e);
            status = 500;
            body = new ErrorView(path, status, "erupt exception", e.getMessage());
        } finally {
            SCOPE.remove();
        }
        if (body instanceof String text) return new Response(status, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
        return new Response(status, "application/json; charset=utf-8", Json.write(body).getBytes(StandardCharsets.UTF_8));
    }

    private Object dispatch(Request req, String path, String query) throws Exception {
        if (path.equals(CloudRestApiConst.NODE_HEALTH)) return name;
        if (!path.startsWith(ERUPT_API + "/")) throw new Reply(404);
        // server-to-server only: an Origin header means a browser reached the node directly
        if (req.header().apply("Origin") != null) throw new Reply(403);
        Set<CloudSignature.Enforced> enforced = authenticate(req);
        String erupt = req.header().apply(HEADER_ERUPT);
        for (Route route : routes) {
            Matcher m = route.pattern.matcher(path);
            if (!route.method.equals(req.method()) || !m.matches()) continue;
            if (!enforced.containsAll(route.enforced)) throw new Reply(403);
            String eruptName = decode(m.group(1));
            Entry<?> entry = entries.get(eruptName);
            if (!eruptName.equals(erupt) || entry == null) throw new Reply(404);
            String parent = req.header().apply(HEADER_ERUPT_PARENT);
            if (parent == null) parent = queryParam(query, PARAM_ERUPT_PARENT);
            // nesting is not supported yet, so a parent can only vouch for itself
            if (parent != null && !parent.isEmpty() && !parent.substring(parent.lastIndexOf('.') + 1).equals(eruptName)) throw new Reply(404);
            String userHeader = req.header().apply(HEADER_USER);
            User user = userHeader == null || userHeader.isEmpty() ? null
                    : User.of(Json.parse(new String(Base64.getDecoder().decode(userHeader.strip()), StandardCharsets.UTF_8)));
            SCOPE.set(new Scope(this, user, req.header().apply(HEADER_TOKEN)));
            List<String> args = new ArrayList<>();
            for (int i = 2; i <= m.groupCount(); i++) args.add(decode(m.group(i)));
            return route.handler.handle(entry, req.body(), args);
        }
        throw new Reply(404);
    }

    /** Every call must carry a valid signature (§12.2); its Erupt-Enforced is what it vouches for. */
    private Set<CloudSignature.Enforced> authenticate(Request req) {
        String signature = req.header().apply(CloudSignature.HEADER_SIGNATURE);
        if (signature == null) throw new Reply(403);
        String contentType = req.header().apply("Content-Type");
        boolean multipart = contentType != null && contentType.contains("multipart/form-data");
        String enforced = req.header().apply(CloudSignature.HEADER_ENFORCED);
        String timestamp = req.header().apply(CloudSignature.HEADER_TIMESTAMP);
        if (timestamp == null || !CloudSignature.verify(accessToken, req.method(), req.target(), timestamp, enforced, req.header().apply(HEADER_USER),
                multipart ? null : req.body() == null ? new byte[0] : req.body(), signature, System.currentTimeMillis() / 1000)) {
            throw new Reply(403);
        }
        return CloudSignature.Enforced.parse(enforced);
    }

    // ---------------------------------------------------------------- endpoints

    private Object build(Entry<?> e, byte[] body, List<String> args) {
        return e.model.buildModel(power(e.model));
    }

    private Object table(Entry<?> e, byte[] body, List<String> args) {
        require(e.model, Right.QUERY);
        return page(e, readMap(body));
    }

    private <T> Object page(Entry<T> e, Map<?, ?> body) {
        int pageIndex = Math.max(toInt(body.get(LambdaSee.field(PageView::pageIndex)), 1), 1);
        int pageSize = Math.min(Math.max(toInt(body.get(LambdaSee.field(PageView::pageSize)), 10), 1), MAX_PAGE_SIZE);
        Repository.Page<T> page = e.repo.page(query(e.model, body, (pageIndex - 1) * pageSize, pageSize));
        List<Map<String, Object>> rows = rows(e.model, page);
        return new PageView(pageIndex, pageSize, page.total(), (page.total() + pageSize - 1) / pageSize, rows, null, null, null);
    }

    // the rows of a page as the table shows them: after DataProxy.afterFetch
    private static <T> List<Map<String, Object>> rows(Model<T> model, Repository.Page<T> page) {
        List<Map<String, Object>> rows = new ArrayList<>(page.records().stream().map(model::toRow).toList());
        model.proxies(p -> p.afterFetch(rows));
        return rows;
    }

    /**
     * {@code POST /data/aggregate/{erupt}}: the {@code @View(statistic)} of each column over every row the table query
     * matches, not only the page shown; a column's total is null beyond {@link #MAX_AGGREGATE_ROWS} rows rather than
     * computed over a part of them. As erupt-core's EruptService.getEruptAggregate answers it.
     */
    private <T> Object aggregate(Entry<T> e, byte[] body, List<String> args) {
        require(e.model, Right.QUERY);
        Map<String, View.Statistic> statistics = new LinkedHashMap<>();
        for (FieldDef f : e.model.fields.values()) {
            for (View v : f.eruptField.views()) {
                if (v.statistic() != View.Statistic.NONE && v.column().isEmpty()) statistics.put(f.name, v.statistic());
            }
        }
        if (statistics.isEmpty()) return Map.of();
        Repository.Page<T> page = e.repo.page(query(e.model, readMap(body), 0, MAX_AGGREGATE_ROWS));
        List<Map<String, Object>> rows = page.total() > MAX_AGGREGATE_ROWS ? null : rows(e.model, page);
        Map<String, Object> totals = new LinkedHashMap<>();
        statistics.forEach((field, statistic) -> totals.put(field, rows == null ? null : Values.aggregate(rows.stream().map(r -> r.get(field)).toList(), statistic)));
        return totals;
    }

    // what the table query asks of the repository: conditions, sort (asked, else the view's, else the model's), filters
    private <T> Query query(Model<T> model, Map<?, ?> body, int offset, int limit) {
        List<Condition> conditions = new ArrayList<>();
        for (Object c : list(body.get(LambdaSee.field(TableQuery::condition)))) conditions.add(condition(model, c instanceof Map<?, ?> m ? m : Map.of()));
        List<Sort> sort = new ArrayList<>();
        for (Object o : list(body.get(LambdaSee.field(TableQuery::sort)))) {
            Map<?, ?> s = o instanceof Map<?, ?> m ? m : Map.of();
            Object field = s.get(LambdaSee.field(Sort::getField));
            // the server checked the sort; a name still only reaches the query as one of the model's columns
            FieldDef f = model.fields.get(String.valueOf(field));
            if (f == null || !f.stored) throw new EruptException("illegal sort field '" + field + "'");
            sort.add(new Sort(f.name, Direction.DESC.name().equalsIgnoreCase(String.valueOf(s.get(LambdaSee.field(Sort::getDirection)))) ? Direction.DESC : Direction.ASC));
        }
        // the @Vis view the table is read for: its filter, and its order when none is asked for
        Vis vis = model.vis(String.valueOf(body.get(LambdaSee.field(TableQuery::vis))));
        if (vis != null && sort.isEmpty()) {
            for (xyz.erupt.annotation.sub_erupt.Sort s : vis.orderBy()) sort.add(new Sort(s.field(), s.direction()));
        }
        if (sort.isEmpty()) sort.addAll(model.orderBy);
        List<String> filters = new ArrayList<>();
        drill(body.get(LambdaSee.field(TableQuery::drill))).ifPresent(filters::add);
        if (vis != null) for (Filter f : vis.filter()) filters.add(f.value());
        model.proxies(p -> Optional.ofNullable(p.beforeFetch(conditions)).filter(s -> !s.isBlank()).ifPresent(filters::add));
        return new Query(conditions, sort, offset, limit, filters);
    }

    /**
     * A drill into this table (§12.4): the server already added the join condition, the drill's {@code linkCondition}
     * is the node's query language. {@code drill} is {@code {erupt, code}}, null for none.
     */
    private Optional<String> drill(Object drill) {
        if (!(drill instanceof Map<?, ?> d)) return Optional.empty();
        Entry<?> source = entries.get(String.valueOf(d.get(LambdaSee.field(DrillRef::erupt))));
        String code = String.valueOf(d.get(LambdaSee.field(DrillRef::code)));
        Drill declared = source == null ? null : Arrays.stream(source.model.erupt.drills()).filter(it -> code.equals(Model.code(it))).findFirst().orElse(null);
        if (declared == null) throw new Reply(403);
        return Optional.of(declared.link().linkCondition()).filter(s -> !s.isEmpty());
    }

    // the server checked the condition and normalized its value (§12.4); the key still only reaches the query as a column
    private static Condition condition(Model<?> model, Map<?, ?> c) {
        Object key = c.get(LambdaSee.field(Condition::getKey)), expression = c.get(LambdaSee.field(Condition::getExpression));
        FieldDef f = model.fields.get(String.valueOf(key));
        if (f == null || !f.stored) throw new EruptException("illegal search field '" + key + "'");
        QueryExpression op;
        try {
            op = QueryExpression.valueOf(String.valueOf(expression));
        } catch (IllegalArgumentException e) {
            throw new EruptException("illegal operator '" + expression + "'");
        }
        Object value = c.get(LambdaSee.field(Condition::getValue));
        if (op == QueryExpression.NULL || op == QueryExpression.NOT_NULL) return new Condition(f.name, null, op);
        // a List / Map field matches its JSON text, one option (or fragment) at a time
        if (f.json) {
            List<?> l = value instanceof List<?> list ? list : List.of(value);
            if (!Set.of(QueryExpression.EQ, QueryExpression.LIKE, QueryExpression.IN).contains(op) || l.size() != 1) {
                throw new EruptException("illegal operator '" + op + "' for " + f.name);
            }
            try {
                return new Condition(f.name, f.toCondition(l.get(0)), QueryExpression.LIKE);
            } catch (IllegalArgumentException e) {
                throw new EruptException(e.getMessage());
            }
        }
        if (op == QueryExpression.LIKE || op == QueryExpression.NOT_LIKE) return new Condition(f.name, Values.text(value), op);
        try {
            if (op == QueryExpression.RANGE || op == QueryExpression.IN || op == QueryExpression.NOT_IN) {
                return new Condition(f.name, list(value).stream().map(f::toCondition).toList(), op);
            }
            return new Condition(f.name, f.toCondition(value), op);
        } catch (IllegalArgumentException e) {
            throw new EruptException(e.getMessage());
        }
    }

    private <T> Object initValue(Entry<T> e, byte[] body, List<String> args) {
        require(e.model, Right.ADD);
        T record = e.model.newInstance();
        e.model.proxies(p -> p.addBehavior(record));
        return e.model.toRow(record);
    }

    private <T> Object get(Entry<T> e, byte[] body, List<String> args) {
        Map<String, Object> power = power(e.model);
        if (!(Boolean) power.get(Right.EDIT.key) && !(Boolean) power.get(Right.VIEW_DETAILS.key)) throw new Reply(403);
        T record = e.repo.get(pk(e.model, args.get(0)));
        if (record == null) throw new EruptException("record not found");
        e.model.proxies(p -> p.editBehavior(record));
        return e.model.toRow(record);
    }

    private <T> Object add(Entry<T> e, byte[] body, List<String> args) {
        require(e.model, Right.ADD);
        T record = e.model.fill(e.model.newInstance(), readMap(body));
        e.model.proxies(p -> p.validate(record));
        e.model.proxies(p -> p.beforeAdd(record));
        e.repo.add(record);
        e.model.proxies(p -> p.afterAdd(record));
        return r(true, null, null);
    }

    private <T> Object update(Entry<T> e, byte[] body, List<String> args) {
        require(e.model, Right.EDIT);
        Map<?, ?> data = readMap(body);
        T record = e.repo.get(pk(e.model, data.get(e.model.pk())));
        if (record == null) throw new EruptException("record not found");
        e.model.fill(record, data);
        e.model.proxies(p -> p.validate(record));
        e.model.proxies(p -> p.beforeUpdate(record));
        e.repo.update(record);
        e.model.proxies(p -> p.afterUpdate(record));
        return r(true, null, null);
    }

    private <T> Object delete(Entry<T> e, byte[] body, List<String> args) {
        require(e.model, Right.DELETE);
        if (!(read(body) instanceof List<?> ids)) throw new EruptException("a list of ids is required");
        List<Object> pks = new ArrayList<>();
        List<T> records = new ArrayList<>();
        for (Object id : ids) {
            Object pk = pk(e.model, id);
            pks.add(pk);
            T record = e.repo.get(pk);
            if (record != null) records.add(record);
        }
        records.forEach(r -> e.model.proxies(p -> p.beforeDelete(r)));
        e.repo.delete(pks);
        records.forEach(r -> e.model.proxies(p -> p.afterDelete(r)));
        return r(true, null, null);
    }

    /**
     * {@code POST /invoke/{erupt}/{kind}/{field}} (§5.5): the node's own code for a field (fetched options, onchange,
     * auto-complete, a button's click), run on the form being filled, and row operations.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Object invoke(Entry<T> e, byte[] body, List<String> args) {
        String kind = args.get(0), target = args.get(1);
        Map<?, ?> data = readMap(body);
        if (kind.equals("operation")) return operation(e, data, target);
        if (kind.equals("operation-form")) return operationFormValue(e, target);
        FieldDef f = e.model.fields.get(target);
        if (f == null) throw new Reply(404);
        T form = e.model.form(data.get("form") instanceof Map<?, ?> m ? m : Map.of());
        Edit edit = f.edit;
        return switch (kind) {
            case "options" -> {
                if (!f.fetchesOptions()) throw new Reply(404);
                yield r(true, null, f.type == EditType.TAGS ? f.tags(form)
                        : f.choices(form).stream().map(o -> Map.of("value", o.get("value"), "label", o.get("label"))).toList());
            }
            case "onchange" -> {
                if (edit.onchange().isInterface()) throw new Reply(404);
                OnChange handler = (OnChange) instance(edit.onchange());
                yield r(true, null, change(null, handler.populateForm(form, edit.onchangeParams()), handler.buildEditExpr(form, edit.onchangeParams())));
            }
            case "autocomplete" -> {
                if (f.type != EditType.AUTO_COMPLETE) throw new Reply(404);
                String typed = Values.text(data.get("value")).toLowerCase(Locale.ROOT);
                List<Object> found = new ArrayList<>();
                for (String v : edit.autoCompleteType().values()) if (v.toLowerCase(Locale.ROOT).contains(typed)) found.add(v);
                if (!edit.autoCompleteType().handler().isInterface()) {
                    List<Object> more = ((AutoCompleteHandler) instance(edit.autoCompleteType().handler()))
                            .completeHandler(form, Values.text(data.get("value")), edit.autoCompleteType().param());
                    if (more != null) found.addAll(more);
                }
                yield r(true, null, found);
            }
            case "button" -> {
                if (f.type != EditType.BUTTON || edit.buttonType().handler().isInterface()) throw new Reply(404);
                EruptButtonHandler handler = (EruptButtonHandler) instance(edit.buttonType().handler());
                String[] params = edit.buttonType().handlerParams();
                yield r(true, null, change(handler.click(form, params), handler.populateForm(form, params), handler.buildEditExpr(form, params)));
            }
            default -> throw new Reply(404);
        };
    }

    // what the browser does after a field's code ran: a script to evaluate, form values to set, edit expressions; null
    // when the code changes nothing
    private static Map<String, Object> change(String eval, Object formData, Object editExpr) {
        if (eval == null && formData == null && editExpr == null) return null;
        Map<String, Object> change = new LinkedHashMap<>();
        if (eval != null) change.put("eval", eval);
        change.put("formData", formData);
        change.put("editExpr", editExpr);
        return change;
    }

    private RowOperation operationOf(Model<?> model, String code) {
        RowOperation op = model.operations().get(code);
        if (op != null) return op;
        // a hidden operation does not exist for this user, exactly like the Java proxy filters it out
        boolean declared = Arrays.stream(model.erupt.rowOperation()).anyMatch(o -> code.equals(o.code().isEmpty() ? Integer.toString(o.title().hashCode()) : o.code()));
        throw new Reply(declared ? 403 : 404);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Object operationFormValue(Entry<T> e, String code) {
        RowOperation op = operationOf(e.model, code);
        if (op.eruptClass() == void.class) return r(true, null, null);
        Model form = model(op.eruptClass());
        Object value = form.newInstance();
        if (!op.operationHandler().isInterface()) {
            Object given = ((OperationHandler) instance(op.operationHandler())).eruptFormValue(List.of(), value, op.operationParam());
            if (given != null) value = given;
        }
        return r(true, null, form.toRow(value));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Object operation(Entry<T> e, Map<?, ?> data, String code) {
        RowOperation op = operationOf(e.model, code);
        Object form = null;
        if (op.eruptClass() != void.class) {
            Model formModel = model(op.eruptClass());
            form = formModel.fill(formModel.newInstance(), data.get("param") instanceof Map<?, ?> m ? m : Map.of());
        }
        List<T> rows = new ArrayList<>();
        if (op.mode() != RowOperation.Mode.BUTTON) {
            if (!(data.get("ids") instanceof List<?> ids) || ids.isEmpty()) throw new EruptException("select at least one row");
            if (op.mode() == RowOperation.Mode.SINGLE && ids.size() != 1) throw new EruptException("select exactly one row");
            for (Object id : ids) {
                T record = e.repo.get(pk(e.model, id));
                if (record == null) throw new EruptException("record '" + id + "' not found");
                rows.add(record);
            }
        }
        String script = op.operationHandler().isInterface() ? null
                : ((OperationHandler) instance(op.operationHandler())).exec(rows, form, op.operationParam());
        return script == null || script.isEmpty() ? r(true, "Executed successfully", null) : r(true, null, script);
    }

    private static Object pk(Model<?> model, Object value) {
        try {
            Object pk = model.pkField().fromJson(value);
            if (pk == null) throw new IllegalArgumentException();
            return pk;
        } catch (IllegalArgumentException e) {
            throw new EruptException("illegal id '" + value + "'");
        }
    }

    // ---------------------------------------------------------------- permissions (PROTOCOL.md §12.5)

    // The server enforces the user's rights and the declared ones from its cached build; what a powerHandler
    // switches per user is checked here again
    private void require(Model<?> model, Right right) {
        if (!(Boolean) power(model).get(right.key)) throw new Reply(403);
    }

    private Map<String, Object> power(Model<?> model) {
        Map<String, Object> power = powerMap(model.power());
        UNSUPPORTED_POWER.forEach(it -> power.put(it.key, false));
        return power;
    }

    /** Every right PowerObject carries, whatever the erupt-annotation version. */
    private static Map<String, Object> powerMap(PowerObject p) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (java.lang.reflect.Field f : PowerObject.class.getDeclaredFields()) {
            if (f.getType() != boolean.class || java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                f.setAccessible(true);
                m.put(f.getName(), f.getBoolean(p));
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new IllegalStateException(e);
            }
        }
        return m;
    }

    // ---------------------------------------------------------------- lifecycle

    /**
     * {@link #connect()} for a node mounted on {@code port} of another HTTP stack; when no {@link #address} is set, the
     * server is told this host's interface toward it.
     */
    public synchronized Node connect(int port) {
        return connect(port, null);
    }

    /** {@link #connect(int)} for a node mounted under {@code path} (a context path or mount point, null for none). */
    public synchronized Node connect(int port, String path) {
        if (address == null) address(registrar.localAddress("http", port, path));
        return connect();
    }

    /** Starts registering with the server and heart-beating; for a node mounted in another HTTP stack. */
    public synchronized Node connect() {
        if (address == null) throw new IllegalStateException("set address(...) to where the server reaches this node");
        NodeInfo info = new NodeInfo();
        info.setVersion(VERSION);
        info.setNodeAddress(new String[]{address});
        info.setErupts(List.copyOf(entries.keySet()));
        info.setProtocolVersion(CloudProtocol.V2.wire());
        info.getCapabilities().add(NodeCapability.INVOKE.wire());
        registrar.start(heartbeat, () -> info);
        return this;
    }

    /** Serves on the JDK HTTP server and connects; returns at once. {@link #close} undoes it. */
    public synchronized Node start(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "erupt-node");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            try (exchange) {
                byte[] in = exchange.getRequestBody().readAllBytes();
                URI uri = exchange.getRequestURI();
                Response resp = handle(new Request(exchange.getRequestMethod(), Request.target(null, uri.getRawPath(), uri.getRawQuery()),
                        exchange.getRequestHeaders()::getFirst, in));
                exchange.getResponseHeaders().set("Content-Type", resp.contentType());
                exchange.sendResponseHeaders(resp.status(), resp.body().length == 0 ? -1 : resp.body().length);
                if (resp.body().length > 0) {
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(resp.body());
                    }
                }
            }
        });
        // HTTP first: the server probes /node-health while registering
        server.start();
        connect(port);
        LOG.log(System.Logger.Level.INFO, "erupt node '" + name + "' at " + address + " -> " + String.join(", ", registrar.servers()));
        return this;
    }

    /** {@link #start}, deregistering on JVM shutdown (Ctrl-C, SIGTERM), and blocks until closed. */
    public void serve(String host, int port) throws IOException, InterruptedException {
        start(host, port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "erupt-shutdown"));
        stopped.await();
    }

    /** Deregisters from the server and stops serving (in-flight requests get up to two seconds). */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        registrar.close();
        if (server != null) {
            server.stop(2);
            executor.shutdown();
        }
        stopped.countDown();
    }

    // ---------------------------------------------------------------- helpers

    private static String version() {
        try (InputStream in = Node.class.getResourceAsStream("/META-INF/maven/xyz.erupt/erupt-cloud-node-lite/pom.properties")) {
            if (in == null) return "dev";
            Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }

    private static ApiModel r(boolean success, String message, Object data) {
        return new ApiModel(success, success ? ApiModel.Status.SUCCESS : ApiModel.Status.ERROR, message, data, ApiModel.PromptWay.MESSAGE);
    }

    private static Object read(byte[] body) {
        if (body == null || body.length == 0) return null;
        try {
            return Json.parse(new String(body, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new EruptException("malformed JSON body");
        }
    }

    private static Map<?, ?> readMap(byte[] body) {
        return read(body) instanceof Map<?, ?> m ? m : Map.of();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> l ? l : List.of();
    }

    private static int toInt(Object value, int fallback) {
        try {
            return value == null ? fallback : (int) Double.parseDouble(Values.text(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String decode(String s) {
        return URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static String queryParam(String query, String key) {
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && decode(pair.substring(0, eq)).equals(key)) return decode(pair.substring(eq + 1));
        }
        return null;
    }

    private void route(String method, String regex, Set<CloudSignature.Enforced> enforced, Handler handler) {
        routes.add(new Route(method, Pattern.compile(regex), enforced, handler));
    }

    @FunctionalInterface
    private interface Handler {
        Object handle(Entry<?> entry, byte[] body, List<String> args) throws Exception;
    }

    private record Route(String method, Pattern pattern, Set<CloudSignature.Enforced> enforced, Handler handler) {
    }

    private record Entry<T>(Model<T> model, Repository<T> repo) {
    }

    // wire shapes of erupt-core's classes, which this node cannot depend on; Json writes a record as an object

    /** erupt-core's TableQuery, of which the paging is read with PageView's keys. */
    private record TableQuery(List<?> condition, List<?> sort, String vis, DrillRef drill) {
    }

    /** The drill a v2 table query is read for (§12.4). */
    private record DrillRef(String erupt, String code) {
    }

    /** erupt-core's Page. */
    private record PageView(int pageIndex, int pageSize, long total, long totalPage, List<?> list, Object sort, Object alert,
                            Object extraContent) {
    }

    /** erupt-core's EruptApiModel. */
    private record ApiModel(boolean success, Status status, String message, Object data, PromptWay promptWay) {
        enum Status {SUCCESS, ERROR}

        enum PromptWay {MESSAGE}
    }

    /** Spring's error body, which the server reads from a failing node. */
    private record ErrorView(String path, int status, String error, String message) {
    }

    /** A right of {@link PowerObject}, keyed on the wire by its field name. */
    private enum Right {
        ADD(PowerObject::isAdd), EDIT(PowerObject::isEdit), DELETE(PowerObject::isDelete), QUERY(PowerObject::isQuery),
        VIEW_DETAILS(PowerObject::isViewDetails), EXPORT(PowerObject::isExport), IMPORTABLE(PowerObject::isImportable),
        CELL_EDIT(PowerObject::isCellEdit), PRINT(PowerObject::isPrint), AI(PowerObject::isAi);

        final String key;

        Right(SFunction<PowerObject, Boolean> right) {
            this.key = LambdaSee.field(right);
        }
    }

    private record Scope(Node node, User user, String token) {
    }

    private static final class Reply extends RuntimeException {
        final int status;

        Reply(int status) {
            super(null, null, false, false);
            this.status = status;
        }
    }
}
