package xyz.erupt.ai_canvas.fun;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TableQuery;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Built-in provider: Erupt models queried through the Erupt data SDK.
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

    @Resource
    private EruptService eruptService;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Object verifyTool() {
        return new VerifyTool();
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

    public class VerifyTool {

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
            return GsonFactory.getGson().toJson(eruptService.getEruptData(eruptModel, tableQuery, null));
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
                Call it to render labels, build filter dropdowns, or pick valid condition values.""")
        public String getChoiceOptions(@P("Erupt model class name") String model,
                                       @P("Java field name of the CHOICE field") String field) {
            EruptModel eruptModel = EruptCoreService.getErupt(model);
            if (null == eruptModel) return "Error: unknown model: " + model;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            if (null == fieldModel) return "Error: unknown field: " + field;
            return GsonFactory.getGson().toJson(EruptUtil.getChoiceList(eruptModel, fieldModel.getEruptField().edit()));
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
        return "## " + eruptModel.getEruptName() + "\nPrimary key field: `id`\n\n```json\n"
                + GsonFactory.getGson().toJson(eruptModel) + "\n```";
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

                // Detail by primary key (`id`). Resolves to the nested entity object.
                const row = await Erupt.row('Product', 42);

                // Tree data (tree models only). Resolves to [{id, label, pid, children: [...]}]
                const nodes = await Erupt.tree('Dept');

                // Labels of a CHOICE field, which stores raw values. Resolves to [{value, label}]
                const options = await Erupt.choice('Product', 'status');
                ```

                Write operations — available but see the "Write Rules" below:

                ```javascript
                // Create. Keys are field names; REFERENCE fields as an object with id: {dept: {id: 2}}
                await Erupt.add('Product', {name: 'Keyboard', price: 199, dept: {id: 2}});

                // Update. ALWAYS fetch the full object first, mutate, then submit it back whole —
                // partial objects lose the omitted fields.
                const item = await Erupt.row('Product', 42);
                item.price = 249;
                await Erupt.update('Product', item);

                // Delete by primary key; a single id or an array of ids
                await Erupt.remove('Product', 42);
                await Erupt.remove('Product', [1, 2, 3]);
                ```

                ## Write Rules (critical)

                - Pages are read-only BY DEFAULT. Use `Erupt.add/update/remove` ONLY when the requirement explicitly asks for creating, editing or deleting.
                - Every delete must be guarded by a confirmation dialog (e.g. `ElMessageBox.confirm`) before calling `Erupt.remove`.
                - After a successful write, reload the affected list/detail and show a success message.
                - Writes are permission-checked server-side (the user needs the model's menu and the model must allow the operation); surface rejection errors to the user instead of swallowing them.

                ## Field Key Rules (critical)

                - `condition[].key` and `sort[].field` are Java field names from the model JSON, never database column names or display titles; for REFERENCE fields use dotted paths, e.g. `{"key": "dept.id", "value": 2, "expression": "EQ"}`.
                - Rows returned by `Erupt.table` are FLAT maps — there are NO nested objects:
                  - simple fields → the plain field name: `row.name`
                  - REFERENCE fields → one flattened key per displayed column, joined with underscores: field `dept` with view column `name` → `row.dept_name` (`row.dept` does NOT exist, `row.dept.name` throws)
                  - deeper view columns flatten the same way: column `area.name` on field `dept` → `row.dept_area_name`
                  - a field appears in table rows ONLY if it declares a `views` entry in the model JSON; never read fields without views from table rows
                - `Erupt.row` (detail by id) is different: it returns the nested entity object, so `row.dept.name` style access is correct there — and it is the object shape `Erupt.update` expects.
                - BOOLEAN fields are `true`/`false`; DATE/DATETIME fields are formatted strings.

                ## No Statistics / Analytics (critical)

                This data source has NO aggregation capability — `Erupt.table` returns ONE PAGE of detail rows, never the full dataset, and there is no sum/count/group-by API.
                - NEVER generate statistic or analytics widgets: totals, sums, averages, ratios, rankings, trend/distribution charts, KPI cards, or any number computed by aggregating fetched rows — they would silently reflect only the current page and be WRONG.
                - The ONLY whole-dataset number available is `total` (record count) from the `Erupt.table` response; showing it is fine.
                - If the requirement asks for statistics or charts, build the page WITHOUT those parts (lists, details, forms are fine) — do not fake them from paged data.
                """;
    }

}
