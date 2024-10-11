package xyz.erupt.flow.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaTaskHistory;
import xyz.erupt.flow.service.ProcessActivityHistoryService;
import xyz.erupt.flow.service.TaskHistoryService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProcessActivityHistoryServiceImpl implements ProcessActivityHistoryService, DataProxy<OaProcessActivityHistory> {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<OaProcessActivityHistory> listByProcInstId(Long instId, boolean active) {
        List<OaProcessActivityHistory> list = eruptDao.lambdaQuery(OaProcessActivityHistory.class)
                .eq(OaProcessActivityHistory::getProcessInstId, instId)
                .eq(OaProcessActivityHistory::getActive, active)
                .orderBy(OaProcessActivityHistory::getFinishDate).list();
        //查询任务历史
        list.forEach(h -> {
            List<OaTaskHistory> taskHistories = taskHistoryService.listByActivityId(h.getId());
            h.setTasks(taskHistories);
        });
        return list;
    }

    @Override
    @Transactional
    public OaProcessActivityHistory copyAndSave(OaProcessActivity activity) {
        OaProcessActivityHistory oaTaskHistory = new OaProcessActivityHistory();
        BeanUtils.copyProperties(activity, oaTaskHistory);
        eruptDao.persist(oaTaskHistory);
        return oaTaskHistory;
    }

    @Override
    public List<OaProcessActivityHistory> listFinishedByExecutionId(Long executionId) {
        List<OaProcessActivityHistory> list = eruptDao.lambdaQuery(OaProcessActivityHistory.class)
                .eq(OaProcessActivityHistory::getExecutionId, executionId)
                .eq(OaProcessActivityHistory::getFinished, true)
                .orderBy(OaProcessActivityHistory::getFinishDate).list();
        //查询任务历史
        list.forEach(h -> {
            List<OaTaskHistory> taskHistories = taskHistoryService.listByActivityId(h.getId());
            h.setTasks(taskHistories);
        });
        return list;
    }

    @Override
    public void updateById(OaProcessActivityHistory entity) {
        eruptDao.merge(entity);
    }

    @Override
    public void removeById(Long id) {
        eruptDao.delete(new OaProcessActivityHistory() {{
            this.setId(id);
        }});
    }
}
