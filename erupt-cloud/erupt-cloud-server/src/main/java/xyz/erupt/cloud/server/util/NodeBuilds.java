package xyz.erupt.cloud.server.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.erupt_field.SearchProxy;
import xyz.erupt.core.proxy.erupt_field.ViewProxy;
import xyz.erupt.core.proxy.erupt_field.type.BoolTypeProxy;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * A node's build model made whole on the server (PROTOCOL.md §7.1, §12.3). A v1 node sends its fields fully
 * resolved or nearly so: the attributes a field leaves out get their defaults, quietly. A v2 node sends only what it
 * declares: every {@code @Erupt} and {@code @EruptField} object is completed from the annotation defaults, and one
 * that does not read back as its annotation is refused, naming the attribute.
 *
 * @author YuePeng
 * date 2026/11/05
 */
public final class NodeBuilds {

    // @Transient on Link: a v2 node sends them for the server's drill conditions, the browser never sees them
    private static final List<String> SERVER_ONLY_LINK = List.of(LambdaSee.method(Link::column), LambdaSee.method(Link::joinColumn));

    private NodeBuilds() {
    }

    public static void complete(EruptBuildModel build, CloudProtocol protocol) {
        for (EruptModel model : models(build)) {
            if (protocol == CloudProtocol.V2) {
                completeStrict(model);
            } else {
                completeFields(model);
            }
        }
    }

    /**
     * What a v2 node declares for the server alone, removed from the build the browser reads.
     */
    public static void hideServerOnly(EruptBuildModel build) {
        for (JsonObject link : links(build.getEruptModel())) {
            SERVER_ONLY_LINK.forEach(link::remove);
        }
    }

    // The link of each drill the model declares
    public static List<JsonObject> links(EruptModel model) {
        List<JsonObject> links = new ArrayList<>();
        JsonElement drills = null == model.getEruptJson() ? null : model.getEruptJson().get(LambdaSee.method(Erupt::drills));
        if (null == drills || !drills.isJsonArray()) return links;
        for (JsonElement drill : drills.getAsJsonArray()) {
            if (!drill.isJsonObject()) continue;
            JsonElement link = drill.getAsJsonObject().get(LambdaSee.method(Drill::link));
            if (null != link && link.isJsonObject()) links.add(link.getAsJsonObject());
        }
        return links;
    }

    private static void completeFields(EruptModel model) {
        for (EruptFieldModel field : Optional.ofNullable(model.getEruptFieldModels()).orElse(List.of())) {
            if (null != field.getEruptFieldJson()) {
                field.setEruptFieldJson(AnnotationProcess.completeJson(EruptField.class, field.getEruptFieldJson()));
            }
        }
    }

    private static void completeStrict(EruptModel model) {
        String name = model.getEruptName();
        try {
            model.setEruptJson(AnnotationProcess.completeJsonStrict(Erupt.class,
                    Optional.ofNullable(model.getEruptJson()).orElseGet(JsonObject::new)));
        } catch (AnnotationProcess.AnnotationJsonException e) {
            throw invalid(name, e);
        }
        for (EruptFieldModel field : Optional.ofNullable(model.getEruptFieldModels()).orElse(List.of())) {
            try {
                field.setEruptFieldJson(AnnotationProcess.completeJsonStrict(EruptField.class,
                        Optional.ofNullable(field.getEruptFieldJson()).orElseGet(JsonObject::new)));
                resolveAutos(field);
            } catch (AnnotationProcess.AnnotationJsonException e) {
                throw invalid(name + "." + field.getFieldName(), e);
            }
        }
    }

    /**
     * The AUTO values a local erupt's proxies resolve, resolved alike for a node, which has no Java field to read:
     * the view type ({@link ViewProxy}, by the edit, then by the stored type the node states as its
     * {@code fieldReturnName}), the search operator ({@link SearchProxy}) and the bool type ({@link BoolTypeProxy}).
     * {@code EditType.AUTO} and {@code DateType.AUTO} are the node's to resolve from its own types.
     */
    private static void resolveAutos(EruptFieldModel field) {
        JsonObject json = field.getEruptFieldJson();
        Edit edit = AnnotationProcess.jsonToAnnotation(EruptField.class, json).edit();
        JsonObject editJson = json.getAsJsonObject(LambdaSee.method(EruptField::edit));
        if (null != editJson) {
            replaceAuto(editJson.getAsJsonObject(LambdaSee.method(Edit::search)), LambdaSee.method(Search::operator),
                    SearchProxy.byDate(edit.type() == EditType.DATE).name());
            replaceAuto(editJson.getAsJsonObject(LambdaSee.method(Edit::boolType)), LambdaSee.method(BoolType::type),
                    BoolTypeProxy.byNotNull(edit.notNull()).name());
        }
        JsonElement views = json.get(LambdaSee.method(EruptField::views));
        if (null == views || !views.isJsonArray()) return;
        for (JsonElement view : views.getAsJsonArray()) {
            if (view.isJsonObject()) {
                replaceAuto(view.getAsJsonObject(), LambdaSee.method(View::type), byStoredType(edit, field.getFieldReturnName()).name());
            }
        }
    }

    private static void replaceAuto(JsonObject object, String key, String resolved) {
        if (null == object) return;
        JsonElement value = object.get(key);
        if (null != value && value.isJsonPrimitive() && AUTO.equals(value.getAsString())) object.addProperty(key, resolved);
    }

    private static final String AUTO = "AUTO";

    private static ViewType byStoredType(Edit edit, String fieldReturnName) {
        ViewType byEdit = ViewProxy.byEdit(edit);
        if (null != byEdit) return byEdit;
        if (Boolean.class.getSimpleName().equalsIgnoreCase(fieldReturnName)) return ViewType.BOOLEAN;
        if (Date.class.getSimpleName().equals(fieldReturnName)) {
            return edit.dateType().type() == DateType.Type.DATE_TIME ? ViewType.DATE_TIME : ViewType.DATE;
        }
        if (NUMBER.equals(fieldReturnName)) return ViewType.NUMBER;
        return ViewType.TEXT;
    }

    // fieldReturnName of a numeric field (PROTOCOL.md §7.1)
    private static final String NUMBER = "number";

    // A node bug: shown as the erupt's error so the node author reads which attribute is wrong
    private static EruptWebApiRuntimeException invalid(String where, AnnotationProcess.AnnotationJsonException e) {
        return new EruptWebApiRuntimeException("Invalid build of " + where + ": " + e.getMessage());
    }

    // The models a build carries: its own, its operation forms, combined erupts and, recursively, its tabs
    private static List<EruptModel> models(EruptBuildModel build) {
        List<EruptModel> models = new ArrayList<>();
        if (null == build) return models;
        Optional.ofNullable(build.getEruptModel()).ifPresent(models::add);
        Optional.ofNullable(build.getOperationErupts()).ifPresent(it -> models.addAll(it.values()));
        Optional.ofNullable(build.getCombineErupts()).ifPresent(it -> models.addAll(it.values()));
        Optional.ofNullable(build.getTabErupts()).ifPresent(it -> it.values().forEach(tab -> models.addAll(models(tab))));
        return models;
    }

}
