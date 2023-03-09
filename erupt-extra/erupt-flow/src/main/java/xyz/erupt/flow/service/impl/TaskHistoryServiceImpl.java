package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskHistory;
import xyz.erupt.flow.mapper.OaTaskHistoryMapper;
import xyz.erupt.flow.service.TaskHistoryService;

import java.util.List;

@Service
public class TaskHistoryServiceImpl extends ServiceImpl<OaTaskHistoryMapper, OaTaskHistory>
        implements TaskHistoryService, DataProxy<OaTaskHistory> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaTaskHistory copyAndSave(OaTask task) {
        OaTaskHistory oaTaskHistory = new OaTaskHistory();
        BeanUtils.copyProperties(task, oaTaskHistory);
        super.saveOrUpdate(oaTaskHistory);
        return oaTaskHistory;
    }

    @Override
    public List<OaTaskHistory> listByActivityId(Long activityId) {
        return this.list(new LambdaQueryWrapper<OaTaskHistory>()
                .eq(OaTaskHistory::getActivityId, activityId)
                .orderByAsc(OaTaskHistory::getCompleteSort)
        );
    }
}
