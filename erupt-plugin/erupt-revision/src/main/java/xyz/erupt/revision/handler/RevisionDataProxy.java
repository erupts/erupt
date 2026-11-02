package xyz.erupt.revision.handler;

import com.google.gson.JsonElement;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;
import xyz.erupt.revision.pojo.FieldChange;
import xyz.erupt.revision.pojo.RevisionVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Fills the revision table's "Record" column: the first few shown text fields of the record as
 * they were after the change (before it, for a delete), so a row reads like the record rather
 * than an id. A delete that was restored since says so, as restoring it again is not possible.
 *
 * @author YuePeng
 */
@Component
public class RevisionDataProxy implements DataProxy<EruptRecordRevision> {

    private static final int FIELDS = 3;

    private static final int FIELD_WIDTH = 40;

    @Resource
    private EruptDao eruptDao;

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> row : list) {
            Object changes = row.get("changes");
            Object erupt = row.get("erupt");
            if (null == changes || null == erupt) continue;
            boolean delete = RevisionOperation.DELETE.name().equals(String.valueOf(row.get("operation")));
            String summary = summarize(EruptCoreService.getErupt(erupt.toString()), RevisionVo.parse(changes.toString()), delete);
            if (delete && this.superseded(erupt.toString(), row)) summary = summary + " (" + I18nTranslate.$translate("Restored") + ")";
            row.put("summary", summary);
        }
    }

    // a delete with a later revision of the same record: it came back already
    private boolean superseded(String erupt, Map<String, Object> row) {
        Object recordId = row.get("recordId");
        Object version = row.get("version");
        if (null == recordId || null == version) return false;
        return eruptDao.lambdaQuery(EruptRecordRevision.class)
                .eq(EruptRecordRevision::getErupt, erupt).eq(EruptRecordRevision::getRecordId, recordId.toString())
                .gt(EruptRecordRevision::getVersion, version).count() > 0;
    }

    static String summarize(EruptModel model, List<FieldChange> changes, boolean before) {
        List<String> parts = new ArrayList<>();
        for (FieldChange change : changes) {
            if (parts.size() >= FIELDS) break;
            JsonElement value = before ? change.getBefore() : change.getAfter();
            if (null == value || !value.isJsonPrimitive() || value.getAsString().isBlank()) continue;
            if (null != model) {
                EruptFieldModel field = model.getEruptFieldMap().get(change.getField());
                // only what the table itself shows, and nothing that is not plain text
                if (null == field || !String.class.getSimpleName().equals(field.getFieldReturnName())) continue;
                if (Arrays.stream(field.getEruptField().views()).noneMatch(View::show)) continue;
            }
            String text = value.getAsString();
            parts.add(text.length() > FIELD_WIDTH ? text.substring(0, FIELD_WIDTH) + "…" : text);
        }
        return String.join(" · ", parts);
    }

}
