package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;

import java.util.List;

public interface ProcessActivityHistoryService {

    /**
     * 查询流程实例中的所有活动
     *
     * @param instId
     */
    List<OaProcessActivityHistory> listByProcInstId(Long instId, boolean active);

    /**
     * 复制并更新
     *
     * @param build
     */
    OaProcessActivityHistory copyAndSave(OaProcessActivity build);


    List<OaProcessActivityHistory> listFinishedByExecutionId(Long processInstId);

    void updateById(OaProcessActivityHistory entity);


    void removeById(Long id);
}
