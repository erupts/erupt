package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessInstanceHistoryMapper;
import xyz.erupt.flow.service.ProcessInstanceHistoryService;
import xyz.erupt.upms.service.EruptContextService;

import java.util.Collection;
import java.util.Map;

@Service
public class ProcessInstanceHistoryServiceImpl extends ServiceImpl<OaProcessInstanceHistoryMapper, OaProcessInstanceHistory>
        implements ProcessInstanceHistoryService, DataProxy<OaProcessInstanceHistory> {

    @Autowired
    private EruptContextService eruptContextService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaProcessInstanceHistory copyAndSave(OaProcessInstance procInst) {
        OaProcessInstanceHistory oaProcessInstanceHistory = new OaProcessInstanceHistory();
        BeanUtils.copyProperties(procInst, oaProcessInstanceHistory);//拷贝全部属性
        super.saveOrUpdate(oaProcessInstanceHistory);
        return oaProcessInstanceHistory;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String currentToken = eruptContextService.getCurrentToken();
        list.forEach(m -> {
            //拼接详情链接
            m.put("detailLink", FlowConstant.SERVER_NAME+"/index.html#/detail/"+ m.get("id") +"/null/_/view?_token="+currentToken);
        });
    }
}
