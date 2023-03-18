package xyz.erupt.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.*;

import java.util.List;

public interface ProcessDefinitionService extends IService<OaProcessDefinition> {

    /**
     * 根据formId修改是否禁止
     * @param isStop
     */
    void updateStopByFormId(Long formId, boolean isStop);

    void updateByFormId(OaProcessDefinition update, Long formId);

    /**
     * 根据formId删除流程定义
     * @param id
     */
    void removeByFormId(Long id);

    /**
     * 根据formId启动流程，会启动最新版流程
     * @param procDefId
     * @return
     */
    OaProcessInstance startById(String procDefId, String content);

    /**
     * 根据formId查询最新版
     * @param formId
     * @return
     */
    OaProcessDefinition getLastVersionByFromId(Long formId);

    /**
     * 预览
     * @param
     * @param formContent
     * @return
     */
    List<OaProcessActivityHistory> preview(String formDefId, JSONObject formContent);

    /**
     * 查看流程实例的时间线
     * @param instId
     * @return
     */
    List<OaProcessActivityHistory> preview(Long instId);

    /**
     * 分组查询
     * @param build
     * @return
     */
    List<OaFormGroups> getFormGroups(OaFormGroups build);

    /**
     * 部署新版本
     * @param oaForms
     */
    void deploy(OaForms oaForms);
}
