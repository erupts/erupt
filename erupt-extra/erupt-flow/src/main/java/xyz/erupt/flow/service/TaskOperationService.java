package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskOperation;

import java.util.List;

public interface TaskOperationService {

    /**
     * 记录日志
     */
    public void log(OaTask task, String operation, String remarks);

    public void log(OaTask task, String operation, String remarks, String nodeId, String nodeName);

    public List<OaTaskOperation> listByOperator(String account);
}
