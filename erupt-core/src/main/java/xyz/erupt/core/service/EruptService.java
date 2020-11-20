package xyz.erupt.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.Query;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-02-29
 */
@Service
@Slf4j
public class EruptService {

    private static final int maxPageSize = 500;

    /**
     * @param eruptModel      eruptModel
     * @param tableQueryVo    前端查询对象
     * @param serverCondition 自定义条件
     * @param customCondition 条件字符串
     * @return
     */
    public Page getEruptData(EruptModel eruptModel, TableQueryVo tableQueryVo, List<Condition> serverCondition, String... customCondition) {
        if (EruptUtil.getPowerObject(eruptModel).isQuery()) {
            int pageSize = tableQueryVo.getPageSize();
            if (pageSize > maxPageSize) {
                pageSize = maxPageSize;
            }
            List<Condition> legalConditions = EruptUtil.geneEruptSearchCondition(eruptModel, tableQueryVo.getCondition());
            List<String> conditionStrings = new ArrayList<>();
            {
                //DependTree逻辑
                LinkTree dependTree = eruptModel.getErupt().linkTree();
                if (StringUtils.isNotBlank(dependTree.field())) {
                    if (null == tableQueryVo.getLinkTreeVal()) {
                        //TODO 临时为前端做兼容用，其实应该抛出异常
                        if (dependTree.dependNode()) {
                            return new Page();
                        }
                    } else {
                        EruptModel treeErupt = EruptCoreService.getErupt(eruptModel.getEruptFieldMap().get(dependTree.field()).getFieldReturnName());
                        String pk = treeErupt.getErupt().primaryKeyCol();
                        //TODO 存在sql注入风险
                        conditionStrings.add(dependTree.field() + "." + pk + " = " + tableQueryVo.getLinkTreeVal());
                    }
                }
            }
            conditionStrings.addAll(Arrays.asList(customCondition));
            EruptUtil.handlerDataProxy(eruptModel, (dataProxy -> {
                String condition = dataProxy.beforeFetch();
                if (null != condition) {
                    conditionStrings.add(condition);
                }
            }));
            if (null != serverCondition) {
                legalConditions.addAll(serverCondition);
            }
            Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                    .queryList(eruptModel, new Page(tableQueryVo.getPageIndex(), pageSize, tableQueryVo.getSort()),
                            Query.builder().orderBy(tableQueryVo.getSort()).conditionStrings(conditionStrings).conditions(legalConditions).build());
            if (null != page.getList()) {
                DataHandlerUtil.convertDataToEruptView(eruptModel, page.getList());
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
    public boolean verifyIdPermissions(EruptModel eruptModel, String id) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition(eruptModel.getErupt().primaryKeyCol(), id, Condition.Expression.EQ));
        Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page(0, 1, null),
                        Query.builder().conditions(conditions).build());
        return page.getList().size() > 0;
    }
}
