package xyz.erupt.revision.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.service.EruptRevisionService;

import java.util.List;

/**
 * The Restore button of a delete revision: the record is rebuilt from its snapshot.
 *
 * @author YuePeng
 */
@Component
public class RevisionRestoreHandler implements OperationHandler<EruptRecordRevision, Void> {

    @Resource
    private EruptRevisionService eruptRevisionService;

    @Override
    public String exec(List<EruptRecordRevision> data, Void unused, String[] param) {
        for (EruptRecordRevision row : data) eruptRevisionService.restore(row.getErupt(), row.getRecordId());
        return null;
    }

}
