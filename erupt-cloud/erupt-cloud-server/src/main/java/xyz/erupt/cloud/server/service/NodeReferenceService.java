package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.core.view.CheckboxModel;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.core.view.TreeModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Completes what a node SDK leaves to the server (erupt-cloud/PROTOCOL.md §7.3): a reference sent as its key
 * gets the {id, label} object the form reads and the flattened {@code <field>_<column>} columns the table reads,
 * and a reference tree is built here from the referenced erupt's records.
 * The referenced erupt is read through {@link CloudNodeRouter} with POWER vouched for: the user reaches it
 * through the erupt holding the reference, whose rights the server already checked. A reference that already
 * is an object (a Java node resolves its own) is left as it is.
 *
 * @author YuePeng
 * date 2026/10/15
 */
@Service
public class NodeReferenceService {

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    private static final Set<CloudSignature.Enforced> QUERIED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.QUERY);

    private static final Set<EditType> REFERENCES = EnumSet.of(EditType.REFERENCE_TABLE, EditType.REFERENCE_TREE);

    private static final String LIST = LambdaSee.field(Page::getList);

    private static final int TARGET_PAGE_SIZE = 1000;

    // A referenced erupt larger than this (a tree, an import's lookup) is cut off rather than read without end
    private static final int MAX_TARGET_RECORDS = 10000;

    @Resource
    private CloudNodeRouter cloudNodeRouter;

    /**
     * @param body the node's answer, a Page or one record as {@code resolve} says
     * @return the body with its references resolved
     */
    public String resolve(String nodeName, EruptBuildModel build, boolean page, String body) {
        JsonElement json = JsonParser.parseString(body);
        if (!json.isJsonObject()) return body;
        List<JsonObject> rows = new ArrayList<>();
        if (page) {
            JsonElement list = json.getAsJsonObject().get(LIST);
            if (null == list || !list.isJsonArray()) return body;
            for (JsonElement row : list.getAsJsonArray()) if (row.isJsonObject()) rows.add(row.getAsJsonObject());
        } else {
            rows.add(json.getAsJsonObject());
        }
        Map<String, JsonObject> targets = new HashMap<>();
        for (Reference reference : references(build)) {
            for (JsonObject row : rows) {
                JsonElement key = row.get(reference.field());
                if (null == key || !key.isJsonPrimitive()) continue;
                JsonObject target = targets.computeIfAbsent(reference.target() + EruptConst.DOT + key.getAsString(),
                        it -> fetch(nodeName, reference.target(), key.getAsString()));
                JsonObject value = new JsonObject();
                value.addProperty(reference.id(), key.getAsString());
                value.add(reference.label(), column(target, reference.label()));
                row.add(reference.field(), value);
                for (String column : reference.columns()) {
                    row.add(reference.field() + "_" + column.replace(EruptConst.DOT, "_"), column(target, column));
                }
            }
        }
        return GsonFactory.getGson().toJson(json);
    }

    /**
     * The tree of a REFERENCE_TREE field, built as a local erupt's is ({@link DataHandlerUtil#quoteTree}). A field with
     * a dependField keeps the records whose dependColumn holds {@code dependValue}.
     */
    public Collection<TreeModel> tree(String nodeName, EruptBuildModel build, String fieldName, String dependValue) {
        Edit edit = referenceTree(build, fieldName);
        ReferenceTreeType treeType = edit.referenceTreeType();
        if (StringUtils.isNotBlank(treeType.dependField()) && StringUtils.isBlank(dependValue)) {
            EruptFieldModel depend = field(build, treeType.dependField());
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.select") + " "
                    + (null == depend ? treeType.dependField() : AnnotationProcess.jsonToAnnotation(EruptField.class, depend.getEruptFieldJson()).edit().title()));
        }
        String root = ExprInvoke.getExpr(treeType.rootPid());
        List<JsonObject> records = targets(nodeName, field(build, fieldName).getFieldReturnName());
        if (StringUtils.isNotBlank(treeType.dependField())) records.removeIf(it -> !dependValue.equals(key(it, treeType.dependColumn())));
        return nest(records, treeType.id(), treeType.label(), treeType.pid(), root);
    }

    /**
     * The tree of a @Tree erupt: all its records nested by the tree's id / label / pid. The tree's rootPid is
     * {@code @Transient}, so roots are the records whose parent is empty.
     */
    public Collection<TreeModel> tree(String nodeName, EruptModel model) {
        Tree tree = model.getErupt().tree();
        return nest(targets(nodeName, model.getEruptName().substring(model.getEruptName().lastIndexOf(EruptConst.DOT) + 1)),
                tree.id(), tree.label(), tree.pid(), null);
    }

    /**
     * The tree beside a @LinkTree table: the records its field points at, nested by the field's referenceTreeType,
     * or null when the build does not carry the field (a Java node's plain LinkTree.field), left to the node.
     */
    public Collection<TreeModel> linkTree(String nodeName, EruptBuildModel build, String fieldName) {
        EruptFieldModel model = field(build, fieldName);
        if (null == model) return null;
        ReferenceTreeType treeType = AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).edit().referenceTreeType();
        return nest(targets(nodeName, model.getFieldReturnName()), treeType.id(), treeType.label(), treeType.pid(),
                ExprInvoke.getExpr(treeType.rootPid()));
    }

    /**
     * A REFERENCE_TABLE picker: a page of the erupt the field points at, read through that erupt's own table
     * (signed {@code power,query}, the query checked against its build), its references resolved as a table's are.
     * A field with a dependField keeps the records whose dependColumn holds {@code dependValue}: the column is
     * {@code @Transient}, so it is read from the build a node SDK writes it into. Null when the build does not carry
     * the field, left to the node.
     */
    public String picker(String nodeName, EruptBuildModel build, String fieldName, String dependValue, byte[] body) {
        EruptFieldModel model = field(build, fieldName);
        if (null == model) return null;
        String target = nodeName + EruptConst.DOT + model.getFieldReturnName();
        EruptBuildModel targetBuild = cloudNodeRouter.resolveBuild(target);
        JsonElement query = null == body || body.length == 0 ? new JsonObject() : JsonParser.parseString(new String(body, StandardCharsets.UTF_8));
        if (!query.isJsonObject()) throw new EruptApiErrorTip(R.error(I18nTranslate.$translate("erupt.incorrect_format")));
        String problem = RemoteRequestValidator.validateQuery(targetBuild, query.getAsJsonObject());
        if (null != problem) throw new EruptApiErrorTip(R.error(problem));
        ReferenceTableType tableType = AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).edit().referenceTableType();
        if (StringUtils.isNotBlank(tableType.dependField())) {
            if (StringUtils.isBlank(dependValue)) {
                EruptFieldModel depend = field(build, tableType.dependField());
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.select") + " " + (null == depend ? tableType.dependField()
                        : AnnotationProcess.jsonToAnnotation(EruptField.class, depend.getEruptFieldJson()).edit().title()));
            }
            // added after the user's conditions were checked: the node takes it under the signed query
            JsonObject condition = new JsonObject();
            condition.addProperty(LambdaSee.field(Condition::getKey), tableType.dependColumn().split("\\.")[0]);
            condition.addProperty(LambdaSee.field(Condition::getExpression), QueryExpression.EQ.name());
            condition.addProperty(LambdaSee.field(Condition::getValue), dependValue);
            JsonObject json = query.getAsJsonObject();
            String key = LambdaSee.field(TableQuery::getCondition);
            if (!json.has(key) || !json.get(key).isJsonArray()) json.add(key, new JsonArray());
            json.getAsJsonArray(key).add(condition);
        }
        return resolve(nodeName, targetBuild, true, cloudNodeRouter.proxy(target, HttpMethod.POST,
                EruptRestPath.ERUPT_DATA + "/table" + EruptRestPath.ERUPT_NAME_HOLDER, query, QUERIED));
    }

    /**
     * The tree of a TAB_TREE field: the records it points at, nested by that erupt's own @Tree; null when the field
     * has a filter, the node's query language, or the build does not carry it, both left to the node.
     */
    public Collection<TreeModel> tabTree(String nodeName, EruptBuildModel build, String fieldName) {
        EruptFieldModel model = field(build, fieldName);
        if (null == model || AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).edit().filter().length > 0) return null;
        String target = nodeName + EruptConst.DOT + model.getFieldReturnName();
        Tree tree = EruptModel.remote(target, nodeName, cloudNodeRouter.resolveBuild(target)).getErupt().tree();
        return nest(targets(nodeName, model.getFieldReturnName()), tree.id(), tree.label(), tree.pid(), null);
    }

    /**
     * The options of a CHECKBOX field: each record it points at as {id, label}. The field's checkboxType is
     * {@code @Transient}, so the key is the target's primary key and the label the field's first view column,
     * {@code name} without one.
     */
    public List<CheckboxModel<Object, Object, Object>> checkbox(String nodeName, EruptBuildModel build, String fieldName) {
        EruptFieldModel model = field(build, fieldName);
        if (null == model) throw new EruptWebApiRuntimeException("not found field " + fieldName);
        String target = nodeName + EruptConst.DOT + model.getFieldReturnName();
        String id = EruptModel.remote(target, nodeName, cloudNodeRouter.resolveBuild(target)).getErupt().primaryKeyCol();
        String label = Arrays.stream(AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).views())
                .map(View::column).filter(it -> !it.isEmpty()).findFirst().orElse(AnnotationConst.LABEL);
        List<CheckboxModel<Object, Object, Object>> options = new ArrayList<>();
        for (JsonObject record : targets(nodeName, model.getFieldReturnName())) {
            options.add(new CheckboxModel<>(text(record.get(id)), text(column(record, label)), null));
        }
        return options;
    }

    private static Collection<TreeModel> nest(List<JsonObject> records, String id, String label, String pid, String root) {
        List<TreeModel> nodes = new ArrayList<>();
        for (JsonObject record : records) {
            nodes.add(new TreeModel(text(record.get(id)), text(record.get(label)), pid.isEmpty() ? null : key(record, pid), root));
        }
        return pid.isEmpty() ? nodes : DataHandlerUtil.quoteTree(nodes);
    }

    /**
     * Whether the node builds a reference tree itself: its filter and orderBy are the node's own query language.
     */
    public boolean nodeBuildsTree(EruptBuildModel build, String fieldName) {
        if (null == field(build, fieldName)) return true;
        Edit edit = referenceTree(build, fieldName);
        return edit.filter().length > 0 || StringUtils.isNotBlank(edit.orderBy());
    }

    private Edit referenceTree(EruptBuildModel build, String fieldName) {
        EruptFieldModel model = field(build, fieldName);
        if (null == model) throw new EruptWebApiRuntimeException("not found field " + fieldName);
        Edit edit = AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).edit();
        if (edit.type() != EditType.REFERENCE_TREE) throw new EruptWebApiRuntimeException(fieldName + " is not a reference tree");
        return edit;
    }

    private static EruptFieldModel field(EruptBuildModel build, String fieldName) {
        return Optional.ofNullable(build.getEruptModel().getEruptFieldModels()).orElse(List.of()).stream()
                .filter(it -> it.getFieldName().equals(fieldName) && null != it.getEruptFieldJson()).findFirst().orElse(null);
    }

    /**
     * Label → key of the erupt a reference field points at, as an import maps its cells.
     */
    public Map<String, Object> labels(String nodeName, EruptFieldModel field) {
        Edit edit = field.getEruptField().edit();
        boolean table = edit.type() == EditType.REFERENCE_TABLE;
        String id = table ? edit.referenceTableType().id() : edit.referenceTreeType().id();
        String label = table ? edit.referenceTableType().label() : edit.referenceTreeType().label();
        Map<String, Object> labels = new HashMap<>();
        for (JsonObject record : targets(nodeName, field.getFieldReturnName())) {
            String text = text(column(record, label));
            if (null != text) labels.putIfAbsent(text, text(record.get(id)));
        }
        return labels;
    }

    // The records of a referenced erupt, page by page, at most MAX_TARGET_RECORDS
    private List<JsonObject> targets(String nodeName, String target) {
        List<JsonObject> records = new ArrayList<>();
        for (int index = 1; records.size() < MAX_TARGET_RECORDS; index++) {
            TableQuery query = new TableQuery();
            query.setPageIndex(index);
            query.setPageSize(TARGET_PAGE_SIZE);
            JsonObject page = JsonParser.parseString(cloudNodeRouter.proxy(nodeName + EruptConst.DOT + target, HttpMethod.POST,
                    EruptRestPath.ERUPT_DATA + "/table" + EruptRestPath.ERUPT_NAME_HOLDER, query, VOUCHED)).getAsJsonObject();
            JsonArray list = Optional.ofNullable(page.getAsJsonArray(LIST)).orElseGet(JsonArray::new);
            for (JsonElement row : list) if (row.isJsonObject()) records.add(row.getAsJsonObject());
            if (list.size() < TARGET_PAGE_SIZE) break;
        }
        return records;
    }

    private JsonObject fetch(String nodeName, String target, String id) {
        try {
            JsonElement record = JsonParser.parseString(cloudNodeRouter.proxy(nodeName + EruptConst.DOT + target, HttpMethod.GET,
                    EruptRestPath.ERUPT_DATA + EruptRestPath.ERUPT_NAME_HOLDER + "/" + UriUtils.encodePathSegment(id, StandardCharsets.UTF_8),
                    null, VOUCHED));
            return record.isJsonObject() ? record.getAsJsonObject() : new JsonObject();
        } catch (EruptWebApiRuntimeException e) {
            // a dangling key: shown without its label rather than failing the whole page
            return new JsonObject();
        }
    }

    // A column of a record, "a.b" walking nested objects
    private static JsonElement column(JsonObject record, String path) {
        JsonElement value = record;
        for (String part : path.split("\\.")) {
            if (null == value || !value.isJsonObject()) return null;
            value = value.getAsJsonObject().get(part);
        }
        return value;
    }

    // The key a column path names: "parent.id" of a reference sent as its key is that key
    private static String key(JsonObject record, String path) {
        JsonElement value = record;
        for (String part : path.split("\\.")) {
            if (null == value || !value.isJsonObject()) break;
            value = value.getAsJsonObject().get(part);
        }
        return text(value);
    }

    private static String text(JsonElement value) {
        return null == value || value.isJsonNull() ? null : value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }

    private static List<Reference> references(EruptBuildModel build) {
        List<Reference> references = new ArrayList<>();
        for (EruptFieldModel model : Optional.ofNullable(build.getEruptModel().getEruptFieldModels()).orElse(List.of())) {
            if (null == model.getEruptFieldJson()) continue;
            EruptField field = AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson());
            Edit edit = field.edit();
            if (!REFERENCES.contains(edit.type())) continue;
            boolean table = edit.type() == EditType.REFERENCE_TABLE;
            List<String> columns = Arrays.stream(field.views()).map(View::column).filter(it -> !it.isEmpty()).toList();
            references.add(new Reference(model.getFieldName(), model.getFieldReturnName(),
                    table ? edit.referenceTableType().id() : edit.referenceTreeType().id(),
                    table ? edit.referenceTableType().label() : edit.referenceTreeType().label(), columns));
        }
        return references;
    }

    // A reference field: the erupt it points at, the keys of its {id, label} object, the columns it shows
    private record Reference(String field, String target, String id, String label, List<String> columns) {
    }

}
