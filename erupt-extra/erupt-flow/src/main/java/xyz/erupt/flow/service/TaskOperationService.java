package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskOperation;

public interface TaskOperationService extends IService<OaTaskOperation> {

    /**
     * 记录日志
     */
    public void log(OaTask task, String operation, String remarks);

    public void log(OaTask task, String operation, String remarks, String nodeId, String nodeName);
}
