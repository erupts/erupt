package xyz.erupt.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2020-02-29
 */
@Service
@Slf4j
public class EruptService {

    @Resource
    private HttpServletRequest request;

    /**
     * @param eruptModel      eruptModel
     * @param tableQuery      前端查询对象
     * @param serverCondition 自定义条件
     * @param customCondition 条件字符串
     */
    public Page getEruptData(EruptModel eruptModel, TableQuery tableQuery, List<Condition> serverCondition, String... customCondition) {
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        List<Condition> legalConditions = EruptUtil.geneEruptSearchCondition(eruptModel, tableQuery.getCondition());
        List<String> conditionStrings = new ArrayList<>();
        //DependTree logic
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field())) {
            if (null == tableQuery.getLinkTreeVal()) {
                if (dependTree.dependNode()) return new Page();
            } else {
                EruptModel treeErupt = EruptCoreService.getErupt(ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName());
                EruptFieldModel pk = treeErupt.getEruptFieldMap().get(treeErupt.getErupt().primaryKeyCol());
                if (pk.getField().getType() == String.class) {
                    conditionStrings.add(String.format("%s = '%s'", dependTree.field() + EruptConst.DOT + pk.getFieldName(), tableQuery.getLinkTreeVal()));
                } else {
                    conditionStrings.add(String.format("%s = %s", dependTree.field() + EruptConst.DOT + pk.getFieldName(), tableQuery.getLinkTreeVal()));
                }
            }
        }
        Layout layout = eruptModel.getErupt().layout();
        if (Layout.PagingType.FRONT == layout.pagingType() || Layout.PagingType.NONE == layout.pagingType()) {
            tableQuery.setPageSize(layout.pageSizes()[layout.pageSizes().length - 1]);
        }
        this.drillProcess(eruptModel, (link, val) -> {
            conditionStrings.add(String.format(val instanceof String ? "%s = '%s'" : "%s = %s", link.linkErupt().getSimpleName() + EruptConst.DOT + link.joinColumn(), val));
            if (StringUtils.isNotBlank(link.linkCondition())) conditionStrings.add(link.linkCondition());
        });
        conditionStrings.addAll(Arrays.asList(customCondition));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> Optional.ofNullable(dataProxy.beforeFetch(legalConditions)).ifPresent(conditionStrings::add)));
        Optional.ofNullable(serverCondition).ifPresent(legalConditions::addAll);
        Page page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, tableQuery, EruptQuery.builder().orderBy(tableQuery.getSort())
                        .conditionStrings(conditionStrings).conditions(legalConditions).build());
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterFetch(page.getList())));
        Optional.ofNullable(page.getList()).ifPresent(it -> DataHandlerUtil.convertDataToEruptView(eruptModel, it));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> Optional.ofNullable(dataProxy.alert(legalConditions)).ifPresent(page::setAlert)));
        return page;
    }

    @SneakyThrows
    public void drillProcess(EruptModel eruptModel, BiConsumer<Link, Object> consumer) {
        String drill = request.getHeader(EruptReqHeader.DRILL);
        if (null != drill) {
            String drillValue = request.getHeader(EruptReqHeader.DRILL_VALUE);
            String sourceErupt = request.getHeader(EruptReqHeader.DRILL_SOURCE_ERUPT);
            if (null == drillValue || null == sourceErupt) {
                throw new EruptWebApiRuntimeException("Drill Header Illegal ，Lack：" + EruptReqHeader.DRILL_VALUE + "," + EruptReqHeader.DRILL_SOURCE_ERUPT);
            }
            EruptModel sourceModel = EruptCoreService.getErupt(sourceErupt);
            Link link = Stream.of(sourceModel.getErupt().drills()).filter(it -> drill.equals(it.code()))
                    .findFirst().orElseThrow(EruptNoLegalPowerException::new).link();
            if (!link.linkErupt().getSimpleName().equals(eruptModel.getEruptName())) {
                throw new EruptWebApiRuntimeException("Illegal erupt from " + drill);
            }
            Object data = DataProcessorManager.getEruptDataProcessor(sourceModel.getClazz()).findDataById(sourceModel, EruptUtil.toEruptId(sourceModel, drillValue));
            Field field = ReflectUtil.findClassField(sourceModel.getClazz(), link.column());
            field.setAccessible(true);
            Object val = field.get(data);
            consumer.accept(link, val);
        }
    }

    /**
     * 校验id使用权限
     *
     * @param eruptModel eruptModel
     * @param id         标识主键
     */
    public void verifyIdPermissions(EruptModel eruptModel, String id) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition(eruptModel.getErupt().primaryKeyCol(), id, QueryExpression.EQ));
        Page page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page(1, 1),
                        EruptQuery.builder().conditions(conditions).build());
        if (page.getList().isEmpty()) {
            throw new EruptNoLegalPowerException();
        }
    }

}
