package xyz.erupt.cloud.server.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.core.view.EruptBatchCellVo;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptCellVo;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Cell editing of a node's erupt, done by the server for every node: a cell is the row read through the node's
 * get, patched with the one value, checked as a whole record and saved through the node's own update, so the
 * node runs its usual validation and DataProxy hooks and a node only needs the endpoints every node serves.
 * The whole row goes back, since a node may store what an update leaves out as empty.
 *
 * @author YuePeng
 * date 2026/10/16
 */
@Service
public class NodeCellService {

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    private static final Set<CloudSignature.Enforced> RECORDED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD);

    @Resource
    private CloudNodeRouter cloudNodeRouter;

    /**
     * @param build the node's build; its power is what the node declared for cell editing
     * @param body  the {@link EruptCellVo} the table sends
     */
    public R<Void> update(String erupt, EruptBuildModel build, byte[] body) {
        String notEditable = I18nTranslate.$translate("erupt.cell.not_editable");
        if (null == build.getPower() || !build.getPower().isCellEdit()) return R.error(notEditable);
        EruptCellVo cell = GsonFactory.getGson().fromJson(new String(body, StandardCharsets.UTF_8), EruptCellVo.class);
        if (null == cell || StringUtils.isBlank(cell.getId()) || StringUtils.isBlank(cell.getField())) return R.error(notEditable);
        EruptModel model = EruptModel.remote(erupt, erupt.substring(0, erupt.lastIndexOf(EruptConst.DOT)), build);
        EruptFieldModel field = editableField(model, cell.getField());
        if (null == field) return R.error(notEditable + ": " + cell.getField());
        return updateOne(erupt, build, model, field, cell.getId(), cell.getValue());
    }

    /**
     * A batch edit is the cell edit of every selected row in turn. A node is written to one row at a time,
     * so a rejected row leaves the rows before it saved: the message names the row, as it does locally.
     *
     * @param build the node's build; its power is what the node declared for batch editing
     * @param body  the {@link EruptBatchCellVo} the table sends
     */
    public R<Void> updateBatch(String erupt, EruptBuildModel build, byte[] body) {
        String notEditable = I18nTranslate.$translate("erupt.cell.not_editable");
        if (null == build.getPower() || !build.getPower().isBatchEdit()) return R.error(notEditable);
        EruptBatchCellVo cells = GsonFactory.getGson().fromJson(new String(body, StandardCharsets.UTF_8), EruptBatchCellVo.class);
        if (null == cells || null == cells.getIds() || cells.getIds().isEmpty() || StringUtils.isBlank(cells.getField())) {
            return R.error(notEditable);
        }
        EruptModel model = EruptModel.remote(erupt, erupt.substring(0, erupt.lastIndexOf(EruptConst.DOT)), build);
        EruptFieldModel field = editableField(model, cells.getField());
        if (null == field) return R.error(notEditable + ": " + cells.getField());
        for (String id : new LinkedHashSet<>(cells.getIds())) {
            if (StringUtils.isBlank(id)) return R.error(I18nTranslate.$translate("erupt.cell.row_not_exist"));
            R<Void> result = updateOne(erupt, build, model, field, id, cells.getValue());
            if (!result.isSuccess()) return result;
        }
        return R.ok();
    }

    // the rules of a local cell: a form field that opts in and is not read-only in the edit form
    private EruptFieldModel editableField(EruptModel model, String fieldName) {
        EruptFieldModel field = model.getEruptFieldMap().get(fieldName);
        Edit edit = null == field ? null : field.getEruptField().edit();
        if (null == edit || StringUtils.isBlank(edit.title()) || !edit.cellEdit() || edit.readonly().edit()) {
            return null;
        }
        return field;
    }

    private R<Void> updateOne(String erupt, EruptBuildModel build, EruptModel model, EruptFieldModel field, String id, JsonElement value) {
        JsonElement row = JsonParser.parseString(cloudNodeRouter.proxy(erupt, HttpMethod.GET, EruptRestPath.ERUPT_DATA
                + EruptRestPath.ERUPT_NAME_HOLDER + "/" + UriUtils.encodePathSegment(id, StandardCharsets.UTF_8), null, VOUCHED));
        // a node answers a missing row with an error envelope, or nothing: a row is what carries its key
        String pk = model.getErupt().primaryKeyCol();
        if (!row.isJsonObject() || !row.getAsJsonObject().has(pk) || row.getAsJsonObject().get(pk).isJsonNull()) {
            return R.error(I18nTranslate.$translate("erupt.cell.row_not_exist") + ": " + id);
        }
        JsonObject record = row.getAsJsonObject();
        record.add(field.getFieldName(), value);
        R<?> problem = RemoteRequestValidator.validateRecord(build, record);
        if (null != problem) return R.error(problem.getMessage());
        R<?> saved = GsonFactory.getGson().fromJson(cloudNodeRouter.proxy(erupt, HttpMethod.POST,
                EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER + "/update", record, RECORDED), R.class);
        return null == saved || saved.isSuccess() ? R.ok() : R.error(saved.getMessage());
    }

}
