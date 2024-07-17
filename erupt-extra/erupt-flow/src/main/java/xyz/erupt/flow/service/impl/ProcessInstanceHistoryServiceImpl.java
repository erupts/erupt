package xyz.erupt.flow.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.ProcessInstanceHistoryService;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptContextService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

@Service
public class ProcessInstanceHistoryServiceImpl
        implements ProcessInstanceHistoryService, DataProxy<OaProcessInstanceHistory> {

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public OaProcessInstanceHistory copyAndSave(OaProcessInstance procInst) {
        OaProcessInstanceHistory oaProcessInstanceHistory = new OaProcessInstanceHistory();
        BeanUtils.copyProperties(procInst, oaProcessInstanceHistory);//拷贝全部属性
        eruptDao.persist(oaProcessInstanceHistory);
        return oaProcessInstanceHistory;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String currentToken = eruptContextService.getCurrentToken();
        list.forEach(m -> {
            //拼接详情链接
            m.put("detailLink", FlowConstant.SERVER_NAME + "/index.html#/detail/" + m.get("id") + "/null/_/view?_token=" + currentToken);
        });
    }
}
