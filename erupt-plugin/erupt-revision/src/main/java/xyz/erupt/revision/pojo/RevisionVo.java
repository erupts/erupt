package xyz.erupt.revision.pojo;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;

import java.util.Date;
import java.util.List;

/**
 * Revision as sent to the record panel: the operator flattened and the changes parsed.
 */
@Getter
@Setter
public class RevisionVo {

    private Long id;

    private Integer version;

    private RevisionOperation operation;

    private Date createTime;

    private Long userId;

    private String userName;

    private String userAvatar;

    private List<FieldChange> changes;

    public static RevisionVo of(EruptRecordRevision revision) {
        RevisionVo vo = new RevisionVo();
        vo.setId(revision.getId());
        vo.setVersion(revision.getVersion());
        vo.setOperation(revision.getOperation());
        vo.setCreateTime(revision.getCreateTime());
        vo.setUserName(revision.getOperator());
        if (null != revision.getCreateUser()) {
            vo.setUserId(revision.getCreateUser().getId());
            vo.setUserAvatar(revision.getCreateUser().getAvatar());
            if (null == vo.getUserName()) vo.setUserName(revision.getCreateUser().getName());
        }
        vo.setChanges(parse(revision.getChanges()));
        return vo;
    }

    // the stored JSON carries explicit nulls; readers expect an absent side as Java null
    public static List<FieldChange> parse(String changes) {
        if (null == changes || changes.isEmpty()) return List.of();
        List<FieldChange> list = GsonFactory.getGson().fromJson(changes, new TypeToken<List<FieldChange>>() {
        }.getType());
        for (FieldChange change : list) {
            if (null != change.getBefore() && change.getBefore().isJsonNull()) change.setBefore(null);
            if (null != change.getAfter() && change.getAfter().isJsonNull()) change.setAfter(null);
        }
        return list;
    }
}
