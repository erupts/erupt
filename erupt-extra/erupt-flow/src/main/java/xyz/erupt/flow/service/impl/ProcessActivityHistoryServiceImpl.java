package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaTaskHistory;
import xyz.erupt.flow.mapper.OaProcessActivityHistoryMapper;
import xyz.erupt.flow.service.ProcessActivityHistoryService;
import xyz.erupt.flow.service.TaskHistoryService;

import java.util.List;

@Service
public class ProcessActivityHistoryServiceImpl extends ServiceImpl<OaProcessActivityHistoryMapper, OaProcessActivityHistory>
        implements ProcessActivityHistoryService, DataProxy<OaProcessActivityHistory> {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Override
    public List<OaProcessActivityHistory> listByProcInstId(Long instId, boolean active) {
        LambdaQueryWrapper<OaProcessActivityHistory> queryWrapper = new LambdaQueryWrapper<OaProcessActivityHistory>()
                .eq(OaProcessActivityHistory::getProcessInstId, instId)
                .eq(OaProcessActivityHistory::getActive, active)
                .orderByAsc(OaProcessActivityHistory::getFinishDate);
        List<OaProcessActivityHistory> list = super.list(queryWrapper);
        //查询任务历史
        list.forEach(h -> {
            List<OaTaskHistory> taskHistories = taskHistoryService.listByActivityId(h.getId());
            h.setTasks(taskHistories);
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaProcessActivityHistory copyAndSave(OaProcessActivity activity) {
        OaProcessActivityHistory oaTaskHistory = new OaProcessActivityHistory();
        BeanUtils.copyProperties(activity, oaTaskHistory);
        super.saveOrUpdate(oaTaskHistory);
        return oaTaskHistory;
    }

    @Override
    public List<OaProcessActivityHistory> listFinishedByExecutionId(Long executionId) {
        LambdaQueryWrapper<OaProcessActivityHistory> queryWrapper = new LambdaQueryWrapper<OaProcessActivityHistory>()
                .eq(OaProcessActivityHistory::getExecutionId, executionId)
                .eq(OaProcessActivityHistory::getFinished, true)
                .orderByAsc(OaProcessActivityHistory::getFinishDate);
        List<OaProcessActivityHistory> list = super.list(queryWrapper);
        //查询任务历史
        list.forEach(h -> {
            List<OaTaskHistory> taskHistories = taskHistoryService.listByActivityId(h.getId());
            h.setTasks(taskHistories);
        });
        return list;
    }
}
