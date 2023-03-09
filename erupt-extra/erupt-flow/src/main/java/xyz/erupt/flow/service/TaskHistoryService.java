package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskHistory;

import java.util.List;

public interface TaskHistoryService extends IService<OaTaskHistory> {

    /**
     * 拷贝并保存
     * @param task
     * @return
     */
    public OaTaskHistory copyAndSave(OaTask task);

    /**
     *
     * @param id
     * @return
     */
    List<OaTaskHistory> listByActivityId(Long id);
}
