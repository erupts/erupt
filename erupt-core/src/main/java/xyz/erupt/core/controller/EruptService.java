package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-29
 */
@Service
public class EruptService {

    private static final int maxPageSize = 500;

    /**
     * @param eruptModel
     * @param pageIndex       index
     * @param pageSize        size
     * @param sort            sort
     * @param searchCondition 客户端查询条件
     * @param joCondition     后台自定义条件
     * @param customCondition 后台自定义条件（字符串格式）
     * @return
     */
    Page getEruptData(EruptModel eruptModel, int pageIndex, int pageSize, String sort,
                      JsonObject searchCondition, JsonObject joCondition, String... customCondition) {
        if (eruptModel.getErupt().power().query()) {
            if (pageSize > maxPageSize) {
                pageSize = maxPageSize;
            }
            JsonObject legalJsonObject = EruptUtil.geneEruptSearchCondition(eruptModel, searchCondition);
            List<String> conditionList = new ArrayList<>();
            conditionList.addAll(Arrays.asList(customCondition));
            EruptUtil.handlerDataProxy(eruptModel, (dataProxy -> {
                String condition = dataProxy.beforeFetch(legalJsonObject);
                if (null != condition) {
                    conditionList.add(condition);
                }
            }));
            if (null != joCondition) {
                for (String key : joCondition.keySet()) {
                    legalJsonObject.add(key, joCondition.get(key));
                }
            }
            Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                    .queryList(eruptModel, new Page(pageIndex, pageSize, sort),
                            legalJsonObject, conditionList.toArray(new String[conditionList.size()]));
            for (Map<String, Object> map : page.getList()) {
                DataHandlerUtil.convertDataToEruptView(eruptModel, map);
            }
            EruptUtil.handlerDataProxy(eruptModel, (dataProxy -> dataProxy.afterFetch(page.getList())));
            return page;
        } else {
            throw new EruptNoLegalPowerException();
        }
    }


    /**
     * 校验id使用权限
     *
     * @param eruptModel erupt Object
     * @param id         标识主键
     * @return
     */
    boolean verifyIdPermissions(EruptModel eruptModel, String id) {
        JsonObject jo = new JsonObject();
        jo.addProperty(eruptModel.getErupt().primaryKeyCol(), id);
        Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page(0, 1, null), jo);
        return page.getList().size() > 0;
    }
}
