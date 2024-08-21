package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskHistory;

import java.util.Collection;
import java.util.List;

public interface TaskHistoryService extends IService<OaTaskHistory> {

    /**
     * 拷贝并保存
     * @param task
     * @return
     */
    public OaTaskHistory copyAndSave(OaTask task);

    /**
     * 拷贝并保存
     * @param tasks
     * @return
     */
    public List<OaTaskHistory> copyAndSave(Collection<OaTask> tasks);

    /**
     *
     * @param id
     * @return
     */
    List<OaTaskHistory> listByActivityId(Long id);
}
