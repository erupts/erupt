package xyz.erupt.core.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.query.Aggregate;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.*;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
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
     * @param tableQuery      Front-end query object
     * @param serverCondition Custom conditions
     * @param customCondition Condition string
     */
    public Page getEruptData(EruptModel eruptModel, TableQuery tableQuery, List<Condition> serverCondition, String... customCondition) {
        // erupt-cloud: forward the list query to the owning node, which runs its own filters/permissions
        if (eruptModel.isRemote()) {
            return EruptRemoteRouterManager.get().tableQuery(eruptModel.getEruptName(), tableQuery);
        }
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        EruptQuery eruptQuery = this.assembleQuery(eruptModel, tableQuery, serverCondition, customCondition);
        // a dependent link tree without a selected node has nothing to list
        if (null == eruptQuery) return new Page();
        Page page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).queryList(eruptModel, tableQuery, eruptQuery);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterFetch(page.getList())));
        Optional.ofNullable(page.getList()).ifPresent(it -> DataHandlerUtil.convertDataToEruptView(eruptModel, it));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> Optional.ofNullable(dataProxy.alert(eruptQuery.getConditions())).ifPresent(page::setAlert)));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> Optional.ofNullable(dataProxy.extraContent(eruptQuery.getConditions(), page.getList())).ifPresent(page::setExtraContent)));
        return page;
    }


    /**
     * Column totals for the rows a list query matches: every {@code @View(statistic = ...)}
     * column, aggregated by the data source under the same conditions, filters and
     * permissions as the list itself. Empty when the model declares no statistic, the
     * source cannot aggregate, or the model lives on a remote node.
     */
    public Map<String, Object> getEruptAggregate(EruptModel eruptModel, TableQuery tableQuery) {
        if (eruptModel.isRemote()) return Collections.emptyMap();
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        List<Aggregate> aggregates = new ArrayList<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            for (View view : fieldModel.getEruptField().views()) {
                if (View.Statistic.NONE == view.statistic()) continue;
                String field = fieldModel.getFieldName();
                // a reference view names a property of the referenced entity; its row key joins the two with "_"
                String path = view.column().isEmpty() ? field : field + EruptConst.DOT + view.column();
                String key = view.column().isEmpty() ? field : field + "_" + view.column().replace(EruptConst.DOT, "_");
                aggregates.add(new Aggregate(path, key, view.statistic()));
            }
        }
        if (aggregates.isEmpty()) return Collections.emptyMap();
        EruptQuery eruptQuery = this.assembleQuery(eruptModel, tableQuery, null);
        if (null == eruptQuery) return Collections.emptyMap();
        Map<String, Object> result = new LinkedHashMap<>();
        Optional.ofNullable(DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).aggregate(eruptModel, eruptQuery, aggregates))
                .ifPresent(result::putAll);
        // columns the source could not aggregate (transient fields a DataProxy fills in afterFetch)
        // are computed over the result as the list endpoint would deliver it. That is a full
        // fetch, so it is bounded: beyond the cap the total is reported as unknown (null)
        // rather than computed over a partial set, and persistent columns are unaffected.
        List<Aggregate> missing = aggregates.stream().filter(a -> !result.containsKey(a.getKey())).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            TableQuery all = new TableQuery();
            all.setCondition(tableQuery.getCondition());
            all.setLinkTreeVal(tableQuery.getLinkTreeVal());
            all.setVis(tableQuery.getVis());
            all.setPageIndex(1);
            all.setPageSize(AGGREGATE_FALLBACK_ROWS);
            Page page = this.getEruptData(eruptModel, all, null);
            boolean complete = null != page.getTotal() && page.getTotal() <= AGGREGATE_FALLBACK_ROWS;
            List<Map<String, Object>> list = new ArrayList<>(Optional.ofNullable(page.getList()).orElse(Collections.emptyList()));
            for (Aggregate aggregate : missing) {
                result.put(aggregate.getKey(), complete ? AggregateUtil.compute(list, aggregate) : null);
            }
        }
        return result;
    }

    // rows an in-memory statistic may scan; a computed column on a bigger result shows no total
    private static final int AGGREGATE_FALLBACK_ROWS = 2000;

    /**
     * The conditions, filters and sort a list request resolves to, shared by the list and
     * aggregate queries; null when a dependent link tree has no selected node.
     */
    private EruptQuery assembleQuery(EruptModel eruptModel, TableQuery tableQuery, List<Condition> serverCondition, String... customCondition) {
        List<Condition> legalConditions = EruptUtil.geneEruptSearchCondition(eruptModel, tableQuery.getCondition());
        List<String> conditionStrings = new ArrayList<>();
        //DependTree logic
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field())) {
            if (null == tableQuery.getLinkTreeVal()) {
                if (dependTree.dependNode()) return null;
            } else {
                EruptModel treeErupt = EruptCoreService.getErupt(ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName());
                EruptFieldModel pk = treeErupt.getEruptFieldMap().get(treeErupt.getErupt().primaryKeyCol());
                conditionStrings.add(String.format("%s in (%s)", dependTree.field() + EruptConst.DOT + pk.getFieldName(),
                        TypeUtil.arrayToConditonString(tableQuery.getLinkTreeVal(), pk.getField().getType())));
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
        if (null != tableQuery.getVis()) {
            for (Vis vis : eruptModel.getErupt().vis()) {
                if (vis.code().equals(tableQuery.getVis())) {
                    for (Filter filter : vis.filter()) {
                        conditionStrings.add(filter.value());
                    }
                    if (vis.orderBy().length > 0) {
                        if (null == tableQuery.getSort() || tableQuery.getSort().isEmpty()) {
                            tableQuery.setSort(new ArrayList<>());
                            for (Sort sort : vis.orderBy()) {
                                tableQuery.getSort().add(new xyz.erupt.annotation.query.Sort(sort.field(), sort.direction()));
                            }
                        }
                    }
                }
            }
        }
        DragSort dragSort = eruptModel.getErupt().dragSort();
        if (StringUtils.isNotBlank(dragSort.field()) && (null == tableQuery.getSort() || tableQuery.getSort().isEmpty())) {
            tableQuery.setSort(new ArrayList<>());
            tableQuery.getSort().add(new xyz.erupt.annotation.query.Sort(dragSort.field(), xyz.erupt.annotation.query.Direction.ASC));
        }
        Optional.ofNullable(serverCondition).ifPresent(legalConditions::addAll);
        return EruptQuery.builder().sort(tableQuery.getSort()).conditionStrings(conditionStrings).conditions(legalConditions).build();
    }

    @SneakyThrows
    public void drillProcess(EruptModel eruptModel, BiConsumer<Link, Object> consumer) {
        // Drill headers only exist on MVC threads; skip when called from non-request
        // contexts such as AI tool execution (langchain4j worker threads)
        if (!EruptSpringUtil.isMvcContext()) return;
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
     * Verify the usage permissions of the ID
     *
     * @param eruptModel eruptModel
     * @param id         PK
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
