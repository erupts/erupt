package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;

import java.util.List;

public interface ProcessActivityHistoryService extends IService<OaProcessActivityHistory> {
    /**
     * 查询流程实例中已完成的活动
     * @param instId
     */
    List<OaProcessActivityHistory> listFinishedActivitiesByProcInstId(Long instId);
}
