package xyz.erupt.ai_canvas.fun;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.ai_canvas.model.AiCanvasModel;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.TableQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Built-in provider: Erupt models read and written through the Erupt data SDK.
 *
 * @author YuePeng
 * date 2026/8/4
 */
@Component
public class EruptCanvasModelProvider implements CanvasModelProvider {

    public static final String TYPE = "erupt";

    // Row cap for verification calls: enough to observe the flattened row keys
    // without flooding the generation context
    private static final int VERIFY_MAX_ROWS = 10;

    // Tree option lookups are not paged; cap the JSON handed back to the LLM
    private static final int VERIFY_MAX_CHARS = 6000;

    @Resource
    private EruptService eruptService;

    // Reference option lookups reuse the controller logic (field filters, depend
    // conditions) so the dry run matches what the page will get at runtime
    @Resource
    private EruptDataController eruptDataController;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Object verifyTool(List<AiCanvasModel> bindings) {
        return new VerifyTool(bindings);
    }

    // Erupt paging is 1-based; models routinely try 0-based indices. Also caps the
    // page size for verification and mirrors the SDK default of EQ for bare conditions
    static void normalizeQuery(TableQuery tableQuery) {
        if (null == tableQuery.getPageIndex() || tableQuery.getPageIndex() < 1) tableQuery.setPageIndex(1);
        if (null == tableQuery.getPageSize() || tableQuery.getPageSize() > VERIFY_MAX_ROWS) {
            tableQuery.setPageSize(VERIFY_MAX_ROWS);
        }
        if (null != tableQuery.getCondition()) {
            tableQuery.getCondition().stream().filter(it -> null == it.getExpression())
                    .forEach(it -> it.setExpression(QueryExpression.EQ));
        }
    }

    /**
     * Write operations a page may run through the SDK. Parsing is lenient about
     * the synonyms LLMs reach for (create / insert, edit, remove).
     */
    public enum WriteOp {
        ADD, UPDATE, DELETE;

        public static WriteOp parse(String operation) {
            if (StringUtils.isBlank(operation)) return null;
            return switch (operation.trim().toLowerCase()) {
                case "add", "create", "insert" -> ADD;
                case "update", "edit", "modify" -> UPDATE;
                case "delete", "remove" -> DELETE;
                default -> null;
            };
        }

        /** Name used by the SDK and by {@link AiCanvasService#allowedWrites} */
        public String sdkName() {
            return switch (this) {
                case ADD -> "add";
                case UPDATE -> "update";
                case DELETE -> "delete";
            };
        }

        public boolean permitted(PowerObject power) {
            return switch (this) {
                case ADD -> power.isAdd();
                case UPDATE -> power.isEdit();
                case DELETE -> power.isDelete();
            };
        }
    }

    // Payload keys the server drops on write: unknown fields, fields without an
    // edit title (never editable) and readonly fields that forbid API changes.
    // Mirrors the copy rules of EruptUtil.dataTarget so the LLM learns which form
    // controls are pointless before embedding them
    static List<String> ignoredKeys(EruptModel eruptModel, WriteOp op, JsonObject data) {
        String pk = eruptModel.getErupt().primaryKeyCol();
        List<String> ignored = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.equals(pk)) continue;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(key);
            if (null == fieldModel) {
                ignored.add(key + " (not a field of the model)");
                continue;
            }
            Edit edit = fieldModel.getEruptField().edit();
            if (AnnotationConst.EMPTY_STR.equals(edit.title())) {
                ignored.add(key + " (not editable)");
                continue;
            }
            Readonly readonly = edit.readonly();
            boolean locked = op == WriteOp.ADD ? readonly.add() : readonly.edit();
            if (locked && !readonly.allowChange()) ignored.add(key + " (readonly)");
        }
        return ignored;
    }

    private static String truncate(String json) {
        if (json.length() <= VERIFY_MAX_CHARS) return json;
        return json.substring(0, VERIFY_MAX_CHARS) + "\n... (truncated, " + json.length() + " chars in total)";
    }


    // The generation request is authorized at login level, so MetaContext carries no erupt model.
    // Power handlers, filters and DataProxy hooks read the model from MetaContext exactly as the
    // runtime /erupt-api/data routes register it, so every dry run runs under the same context.
    static String withErupt(EruptModel eruptModel, java.util.function.Supplier<String> verification) {
        MetaErupt previous = MetaContext.getErupt();
        MetaContext.register(new MetaErupt(eruptModel.getEruptName(), eruptModel.getEruptName()));
        try {
            return verification.get();
        } finally {
            MetaContext.register(previous);
        }
    }

    public class VerifyTool {

        // Canvas bindings of this data source type: the designer's switches are the first gate of every write
        private final List<AiCanvasModel> bindings;

        VerifyTool(List<AiCanvasModel> bindings) {
            this.bindings = null == bindings ? List.of() : bindings;
        }

        private boolean enabledByCanvas(String model, WriteOp op) {
            return bindings.stream().filter(it -> model.equals(it.getModel()))
                    .anyMatch(it -> AiCanvasService.allowedWrites(it).contains(op.sdkName()));
        }

        @Tool("""
                Execute the paged list query the page will run, to VERIFY it works before embedding it.
                Pass EXACTLY the same JSON the page will pass to Erupt.table(model, query):
                {pageIndex, pageSize, sort: [{field, direction}], condition: [{key, value, expression}]}.
                Returns the same page object Erupt.table resolves to (rows are FLAT maps) with at most a few rows,
                or fails with the server error when the query is invalid — fix the query and verify again.""")
        public String verifyTableQuery(@P("Erupt model class name from the Data Model section") String model,
                                       @P("Query JSON, identical to Erupt.table's second argument") String queryJson) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            TableQuery tableQuery = GsonFactory.getGson().fromJson(StringUtils.defaultIfBlank(queryJson, "{}"), TableQuery.class);
            normalizeQuery(tableQuery);
            return withErupt(eruptModel, () -> GsonFactory.getGson().toJson(eruptService.getEruptData(eruptModel, tableQuery, null)));
        }

        @Tool("""
                Get the structure JSON of any Erupt model (fields, edit types, view columns).
                The selected model's structure is already in the prompt — call this only when the page
                touches ANOTHER model (e.g. the target of a REFERENCE field or a related tree model)
                whose structure you have not seen yet.""")
        public String getModelStructure(@P("Erupt model class name") String model) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            return GsonFactory.getGson().toJson(eruptModel);
        }

        @Tool("""
                List the {value, label} options of a CHOICE / MULTI_CHOICE field — table rows store the raw value.
                Call it to render labels, build filter dropdowns or form selects, or pick valid condition values.""")
        public String getChoiceOptions(@P("Erupt model class name") String model,
                                       @P("Java field name of the CHOICE field") String field) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            if (null == fieldModel) return "Error: unknown field: " + field;
            return GsonFactory.getGson().toJson(EruptUtil.getChoiceList(eruptModel, fieldModel.getEruptField().edit()));
        }

        @Tool("""
                Run the option lookup of a REFERENCE_TABLE / REFERENCE_TREE field exactly as the page will
                (Erupt.referenceTable / Erupt.referenceTree), to VERIFY it and observe the option row keys.
                For REFERENCE_TABLE pass the same query JSON as Erupt.table (a few rows are returned);
                for REFERENCE_TREE the query is ignored. dependValue is needed only when the field declares a dependField.""")
        public String verifyReferenceQuery(@P("Erupt model class name that OWNS the reference field") String model,
                                           @P("Java field name of the REFERENCE_TABLE / REFERENCE_TREE field") String field,
                                           @P("Query JSON for REFERENCE_TABLE; empty otherwise") String queryJson,
                                           @P("Value of the depend field; empty when the field declares none") String dependValue) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            if (null == fieldModel) return "Error: unknown field: " + field;
            return withErupt(eruptModel, () -> this.referenceQuery(eruptModel, fieldModel, field, queryJson, dependValue));
        }

        private String referenceQuery(EruptModel eruptModel, EruptFieldModel fieldModel, String field, String queryJson, String dependValue) {
            String model = eruptModel.getEruptName();
            EditType type = fieldModel.getEruptField().edit().type();
            String depend = StringUtils.defaultIfBlank(dependValue, null);
            try {
                if (type == EditType.REFERENCE_TABLE) {
                    TableQuery tableQuery = GsonFactory.getGson().fromJson(StringUtils.defaultIfBlank(queryJson, "{}"), TableQuery.class);
                    normalizeQuery(tableQuery);
                    return GsonFactory.getGson().toJson(eruptDataController.getReferenceTable(model, field, depend, false, tableQuery));
                } else if (type == EditType.REFERENCE_TREE) {
                    return truncate(GsonFactory.getGson().toJson(eruptDataController.getReferenceTree(model, field, depend)));
                }
                return "Error: " + field + " is a " + type + " field, not a REFERENCE_TABLE / REFERENCE_TREE";
            } catch (RuntimeException e) {
                return "Error: " + ExceptionUtils.getRootCauseMessage(e);
            }
        }

        @Tool("""
                Dry-run a write the page will perform — NOTHING is persisted. Checks that the operation is permitted
                on the model for the current user, validates the payload the way the server will (required fields,
                number / regex / length rules, DataProxy validation, field type conversion) and reports payload keys
                the server would silently ignore. operation: add | update | delete.
                payloadJson: for add / update the SAME row object the page passes to Erupt.add / Erupt.update
                (use realistic sample values); for delete the id array passed to Erupt.remove.
                Fails with the server message when the payload is invalid — fix the form and verify again.""")
        public String verifyWrite(@P("Erupt model class name") String model,
                                  @P("add | update | delete") String operation,
                                  @P("Payload JSON, identical to the SDK argument") String payloadJson) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            WriteOp op = WriteOp.parse(operation);
            if (null == op) return "Error: unknown operation: " + operation + " (expected add | update | delete)";
            return withErupt(eruptModel, () -> this.dryRunWrite(eruptModel, op, payloadJson));
        }

        private String dryRunWrite(EruptModel eruptModel, WriteOp op, String payloadJson) {
            String model = eruptModel.getEruptName();
            if (!this.enabledByCanvas(model, op)) {
                return "Error: " + op.sdkName() + " is not enabled for " + model
                        + " in this canvas's model settings — leave this operation out of the page";
            }
            if (!op.permitted(PowerInvoke.getPowerObject(eruptModel))) {
                return "Error: " + op.name().toLowerCase() + " is not permitted on " + model
                        + " (disabled by the model's power config or the current user's role) — leave this operation out of the page";
            }
            JsonElement payload;
            try {
                payload = JsonParser.parseString(StringUtils.defaultIfBlank(payloadJson, "null"));
            } catch (JsonSyntaxException e) {
                return "Error: payload is not valid JSON: " + e.getMessage();
            }
            String pk = eruptModel.getErupt().primaryKeyCol();
            if (op == WriteOp.DELETE) {
                if (!payload.isJsonArray() || payload.getAsJsonArray().isEmpty()) {
                    return "Error: delete payload must be a non-empty array of primary keys, e.g. [1, 2]";
                }
                return "OK: delete of " + payload.getAsJsonArray().size() + " row(s) accepted (not executed)";
            }
            if (!payload.isJsonObject()) return "Error: " + op.name().toLowerCase() + " payload must be a JSON object keyed by field names";
            JsonObject data = payload.getAsJsonObject();
            if (op == WriteOp.UPDATE && (!data.has(pk) || data.get(pk).isJsonNull())) {
                return "Error: update payload must carry the primary key `" + pk
                        + "` — fetch the row with Erupt.row first, mutate it and submit it back whole";
            }
            try {
                R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
                if (!validation.isSuccess()) return "Error: " + validation.getMessage();
                // Same conversion the server applies before persisting; surfaces wrong value shapes
                // (e.g. a plain id where {id} is expected, a non-numeric NUMBER)
                GsonFactory.getGson().fromJson(data, eruptModel.getClazz());
            } catch (RuntimeException e) {
                return "Error: payload rejected: " + ExceptionUtils.getRootCauseMessage(e);
            }
            List<String> ignored = ignoredKeys(eruptModel, op, data);
            String ok = "OK: " + op.name().toLowerCase() + " payload accepted (not executed)";
            return ignored.isEmpty() ? ok : ok + "; the server IGNORES these keys, drop their form controls: " + ignored;
        }
    }

    @Override
    public List<VLModel> models() {
        return EruptCoreService.getErupts().stream()
                .map(it -> new VLModel(it.getEruptName(), I18nTranslate.$translate(it.getErupt().name())))
                .sorted(Comparator.comparing(VLModel::getValue)).collect(Collectors.toList());
    }

    @Override
    public String describe(String model) {
        EruptModel eruptModel = EruptCoreService.getErupt(model);
        if (null == eruptModel) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-canvas.model_not_found") + ": " + model);
        }
        return "## " + eruptModel.getEruptName() + "\nPrimary key field: `" + eruptModel.getErupt().primaryKeyCol()
                + "`\n\n```json\n" + GsonFactory.getGson().toJson(eruptModel) + "\n```";
    }

    @Override
    public String queryGuide() {
        return """
                ## Data Access (SDK only — never hand-roll HTTP)

                The SDK exposes a global `Erupt` object. It already handles the base path, authentication and headers — do NOT use fetch/axios/XHR against `erupt-api` yourself.

                `{Model}` below is the Erupt model class name from the "Data Model" section; every function returns a Promise and rejects with an `Error` on failure.

                ```javascript
                // Paged list. All query fields optional; defaults: pageIndex 1, pageSize 20.
                // pageIndex is 1-BASED: the first page is 1, never 0.
                // Resolves to {pageIndex, pageSize, total, totalPage, list: [row, ...]}
                const page = await Erupt.table('Product', {
                  pageIndex: 1,
                  pageSize: 20,
                  sort: [{field: 'createTime', direction: 'DESC'}],        // direction: 'ASC' | 'DESC'
                  condition: [{key: 'status', value: '1', expression: 'EQ'}]
                  // expression: EQ | NEQ | GT | GTE | LT | LTE | LIKE | NOT_LIKE
                  //           | RANGE (value is [min, max]) | IN | NOT_IN (value is an array) | NULL | NOT_NULL
                });
                // el-table sort-change events ({prop, order: 'ascending'|'descending'}) may be passed
                // into `sort` as-is — the SDK normalizes them.

                // Detail by primary key. Resolves to the row keyed by Java field names — NOT the flat
                // table shape: REFERENCE fields are objects ({id, <label column>, ...}: row.dept.id, row.dept.name),
                // CHECKBOX / MULTI_CHOICE fields are arrays of raw values, DATE fields are strings.
                const row = await Erupt.row('Product', 42);

                // Tree data (tree models only). Resolves to [{id, label, pid, children: [...]}]
                const nodes = await Erupt.tree('Dept');

                // Labels of a CHOICE / MULTI_CHOICE field, which stores raw values. Resolves to [{value, label}]
                const options = await Erupt.choice('Product', 'status');
                ```

                The SDK also has `Erupt.add / update / remove` and form option lookups; they may be used ONLY when a "Data Writes" section is present in this prompt and the model's "Allowed writes" line lists the operation. Without that section every page is strictly read-only.

                ## Field Key Rules (critical)

                - `condition[].key` and `sort[].field` are Java field names from the model JSON, never database column names or display titles; for REFERENCE fields use dotted paths, e.g. `{"key": "dept.id", "value": 2, "expression": "EQ"}`.
                - Rows returned by `Erupt.table` are FLAT maps — there are NO nested objects:
                  - simple fields → the plain field name: `row.name`
                  - REFERENCE fields → one flattened key per displayed column, joined with underscores: field `dept` with view column `name` → `row.dept_name` (`row.dept` does NOT exist, `row.dept.name` throws)
                  - deeper view columns flatten the same way: column `area.name` on field `dept` → `row.dept_area_name`
                  - a field appears in table rows ONLY if it declares a `views` entry in the model JSON; never read fields without views from table rows
                - `Erupt.row` (detail by id) is different: it returns the object described above, so `row.dept.name` style access is correct there — and it is the object shape `Erupt.update` expects back.
                - BOOLEAN fields are `true`/`false`; DATE/DATETIME fields are formatted strings.

                ## No Statistics / Analytics (critical)

                This data source has NO aggregation capability — `Erupt.table` returns ONE PAGE of detail rows, never the full dataset, and there is no sum/count/group-by API.
                - NEVER generate statistic or analytics widgets: totals, sums, averages, ratios, rankings, trend/distribution charts, KPI cards, or any number computed by aggregating fetched rows — they would silently reflect only the current page and be WRONG.
                - The ONLY whole-dataset number available is `total` (record count) from the `Erupt.table` response; showing it is fine.
                - If the requirement asks for statistics or charts, build the page WITHOUT those parts (lists, details, forms are fine) — do not fake them from paged data.
                """;
    }

    @Override
    public String writeGuide() {
        return """
                ## Data Writes (SDK)

                Each model in the "Data Models" section carries an "Allowed writes" line. A page may create / update / delete rows of a model ONLY for the operations listed there; never write to a model whose line says read-only, and never invent other write paths. The server re-checks the visitor's own permissions on every call.

                ```javascript
                // Server-side defaults of a NEW row (default values, addBehavior hooks); start the create form from it.
                // Resolves to a row object keyed by field names (may be empty)
                const draft = await Erupt.initValue('Product');

                // Create. Keys are Java field names; value shapes follow the "Payload Rules" below
                await Erupt.add('Product', {name: 'Keyboard', price: 199, status: '1', dept: {id: 2}});

                // Update. Fetch the row first, mutate it, submit it back WHOLE — the primary key is required,
                // and fields missing from the payload are treated as cleared
                const item = await Erupt.row('Product', 42);
                item.price = 249;
                await Erupt.update('Product', item);

                // Delete by primary key; a single id or an array of ids
                await Erupt.remove('Product', 42);
                await Erupt.remove('Product', [1, 2, 3]);

                // Option lookups for form controls
                // REFERENCE_TABLE: paged rows of the target model narrowed by the field's filter; query as in Erupt.table.
                // Resolves to the same page shape as Erupt.table on the target model (flat rows, primary key included)
                const depts = await Erupt.referenceTable('Product', 'dept', {pageSize: 50, condition: [{key: 'name', value: 'Sales', expression: 'LIKE'}]});
                // REFERENCE_TREE: resolves to [{id, label, pid, children: [...]}]
                const deptTree = await Erupt.referenceTree('Product', 'dept');
                // CHECKBOX: resolves to [{id, label, remark}]
                const roles = await Erupt.checkbox('Product', 'roles');
                // CHOICE / MULTI_CHOICE: resolves to [{value, label}]
                const statuses = await Erupt.choice('Product', 'status');
                // Fields whose reference declares a dependField take the depend value as the last argument
                const cities = await Erupt.referenceTable('Address', 'city', {pageSize: 50}, form.province.id);
                ```

                ## Form Rules

                - Build forms from the model JSON: include every field whose `edit.title` is non-empty and `edit.show` is true; a field with an empty edit title is not editable. Never show the primary key in a create form.
                - Label = `edit.title`; hints from `edit.desc` / `edit.placeHolder`; control by `edit.type` (see the table). Options for CHOICE / MULTI_CHOICE / REFERENCE_* / CHECKBOX fields come from the SDK lookups above — never hard-code them.
                - `edit.notNull` → required (validate client-side before submitting; the server rejects violations too). `edit.readonly.add` → omit from the create form; `edit.readonly.edit` → render disabled in the edit form.
                - Do NOT put fields of type TAB_TABLE_ADD, TAB_TABLE_REFER, TAB_TREE, MULTI_FORM, COMBINE, ATTACHMENT, SIGNATURE, MAP, HTML_EDITOR, CODE_EDITOR, MARKDOWN, TPL, BUTTON, DIVIDE, GROUP, CALLOUT, EMPTY into forms. On update keep whatever `Erupt.row` returned for them untouched so they survive the round-trip; on create leave them out.

                ## Payload Rules (value shape per `edit.type`)

                | edit.type | value in the payload |
                |---|---|
                | INPUT, TEXTAREA, AUTO_COMPLETE, PASSWORD, COLOR | string |
                | NUMBER, SLIDER, RATE | number |
                | BOOLEAN | `true` / `false` |
                | CHOICE | the raw option `value` from `Erupt.choice` (string or number matching the field type) |
                | MULTI_CHOICE | array of raw option values |
                | TAGS | tags joined with `edit.tagsType.joinSeparator`; when the separator is `[]` a JSON array string, e.g. `'["a","b"]'` |
                | DATE | string by `edit.dateType.type`: DATE `yyyy-MM-dd`, DATE_TIME `yyyy-MM-ddTHH:mm:ss`, TIME `HH:mm:ss`, MONTH `yyyy-MM`, YEAR `yyyy` |
                | REFERENCE_TABLE, REFERENCE_TREE | object holding the target's primary key under the key named by `referenceTableType.id` / `referenceTreeType.id` (normally `id`): `{id: 2}` |
                | CHECKBOX | array of `{id}` objects: `[{id: 1}, {id: 3}]` — `Erupt.row` returns plain ids for this type, convert before `Erupt.update` |
                | HIDDEN | keep the value from `Erupt.initValue` / `Erupt.row` as-is |

                ## Write UX Rules (critical)

                - Offer exactly the allowed writes. When the requirement is silent, allowed writes still belong on the page as its natural completion (a "Create" button on the list toolbar, per-row "Edit" / "Delete" actions); when the requirement asks for a write the model does not allow, leave it out and do not fake it.
                - Every delete is guarded by a confirmation dialog (`ElMessageBox.confirm`, destructured from the `ElementPlus` global) before `Erupt.remove`.
                - Submit through a form dialog with client-side validation; after a successful write reload the affected list / detail and show a success message.
                - Surface rejections: show the `message` of the thrown `Error` (validation, permission) instead of swallowing it.

                ## Write Verification (ReAct)

                Every write the page performs MUST be dry-run with the write verification tool before it appears in the document — one call per operation per model, with a realistic payload built exactly as the page will build it (for update: fetch a real row with the list / detail tools first, mutate, submit). Option lookups for REFERENCE fields are verified with the reference verification tool. Only writes that pass may be embedded, and the form must not contain controls for keys the dry run reports as ignored.
                """;
    }

}
