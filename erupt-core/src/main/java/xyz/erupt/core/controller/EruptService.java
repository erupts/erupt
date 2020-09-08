package xyz.erupt.core.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.service.EruptCoreService;
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
@Log
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
        if (EruptUtil.getPowerObject(eruptModel).isQuery()) {
            if (pageSize > maxPageSize) {
                pageSize = maxPageSize;
            }
            List<String> conditionList = new ArrayList<>();
            JsonObject legalJsonObject = EruptUtil.geneEruptSearchCondition(eruptModel, searchCondition);
            {
                //DependTree逻辑
                LinkTree dependTree = eruptModel.getErupt().linkTree();
                if (StringUtils.isNotBlank(dependTree.field())) {
                    JsonElement je = searchCondition.get("$" + dependTree.field());
                    if (null == je || je.isJsonNull()) {
                        //TODO 临时为前端做兼容用，按道理应该抛出异常
                        if (dependTree.dependNode()) {
                            return new Page();
                        }
                    } else {
                        EruptModel treeErupt = EruptCoreService.getErupt(dependTree.field());
                        String pk = treeErupt.getErupt().primaryKeyCol();
                        //TODO 存在sql注入风险
                        conditionList.add(dependTree.field() + "." + pk + " = " + je.getAsString());
//                        legalJsonObject.addProperty(linkTree.field() + "." + pk, Long.valueOf(je.getAsString()));
                    }
                }
            }

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
                            legalJsonObject, conditionList.toArray(new String[0]));
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
