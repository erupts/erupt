package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.core.view.TreeModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Completes what a node SDK leaves to the server (erupt-cloud/PROTOCOL.md §7.3): a reference sent as its key
 * gets the {id, label} object the form reads and the flattened {@code <field>_<column>} columns the table reads,
 * and a reference tree the node does not build (404) is built here from the referenced erupt's records.
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
     * The tree of a REFERENCE_TREE field, built as a local erupt's is ({@link DataHandlerUtil#quoteTree}).
     */
    public Collection<TreeModel> tree(String nodeName, EruptBuildModel build, String fieldName) {
        EruptFieldModel model = build.getEruptModel().getEruptFieldModels().stream()
                .filter(it -> it.getFieldName().equals(fieldName) && null != it.getEruptFieldJson()).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException("not found field " + fieldName));
        Edit edit = AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson()).edit();
        if (edit.type() != EditType.REFERENCE_TREE) throw new EruptWebApiRuntimeException(fieldName + " is not a reference tree");
        ReferenceTreeType treeType = edit.referenceTreeType();
        String root = ExprInvoke.getExpr(treeType.rootPid());
        List<TreeModel> nodes = new ArrayList<>();
        for (JsonObject record : targets(nodeName, model.getFieldReturnName())) {
            nodes.add(new TreeModel(text(record.get(treeType.id())), text(record.get(treeType.label())),
                    treeType.pid().isEmpty() ? null : text(record.get(treeType.pid())), root));
        }
        return treeType.pid().isEmpty() ? nodes : DataHandlerUtil.quoteTree(nodes);
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
