package xyz.erupt.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author YuePeng
 * date 2020-02-29
 */
@Service
@Slf4j
public class EruptService {


    /**
     * @param eruptModel      eruptModel
     * @param tableQueryVo    前端查询对象
     * @param serverCondition 自定义条件
     * @param customCondition 条件字符串
     */
    public Page getEruptData(EruptModel eruptModel, TableQueryVo tableQueryVo, List<Condition> serverCondition, String... customCondition) {
        if (PowerInvoke.getPowerObject(eruptModel).isQuery()) {
            List<Condition> legalConditions = EruptUtil.geneEruptSearchCondition(eruptModel, tableQueryVo.getCondition());
            List<String> conditionStrings = new ArrayList<>();
            {
                //DependTree逻辑
                LinkTree dependTree = eruptModel.getErupt().linkTree();
                if (StringUtils.isNotBlank(dependTree.field())) {
                    if (null == tableQueryVo.getLinkTreeVal()) {
                        if (dependTree.dependNode()) {
                            return new Page();
                        }
                    } else {
                        EruptModel treeErupt = EruptCoreService.getErupt(ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName());
                        String pk = treeErupt.getErupt().primaryKeyCol();
                        conditionStrings.add(dependTree.field() + "." + pk + " = '" + tableQueryVo.getLinkTreeVal() + "'");
                    }
                }
            }
            conditionStrings.addAll(Arrays.asList(customCondition));
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> {
                String condition = dataProxy.beforeFetch(eruptModel.getClazz());
                if (null != condition) {
                    conditionStrings.add(condition);
                }
            }));
            if (null != serverCondition) {
                legalConditions.addAll(serverCondition);
            }
            Page page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                    .queryList(eruptModel, new Page(tableQueryVo.getPageIndex(), tableQueryVo.getPageSize(), tableQueryVo.getSort()),
                            EruptQuery.builder().orderBy(tableQueryVo.getSort()).conditionStrings(conditionStrings).conditions(legalConditions).build());
            if (null != page.getList()) {
                DataHandlerUtil.convertDataToEruptView(eruptModel, page.getList());
            }
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterFetch(page.getList())));
            return page;
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    /**
     * 校验id使用权限
     *
     * @param eruptModel eruptModel
     * @param id         标识主键
     * @return 是否有数据权限
     */
    public boolean verifyIdPermissions(EruptModel eruptModel, String id) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition(eruptModel.getErupt().primaryKeyCol(), id, QueryExpression.EQ));
        Page page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page(0, 1, null),
                        EruptQuery.builder().conditions(conditions).build());
        boolean is = page.getList().size() > 0;
        if (is) {
            log.info(eruptModel.getEruptName() + " Id allows access : " + id);
        }
        return is;
    }

}
