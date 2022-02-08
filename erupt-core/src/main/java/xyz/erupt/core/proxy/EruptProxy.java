package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.proxy.erupt.DrillProxy;
import xyz.erupt.core.proxy.erupt.FilterProxy;
import xyz.erupt.core.proxy.erupt.RowOperationProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptProxy extends AnnotationProxy<Erupt, Void> {

    private final AnnotationProxy<Filter, Erupt> filterProxy = new FilterProxy<>();

    private final AnnotationProxy<Drill, Erupt> drillProxy = new DrillProxy();

    private final AnnotationProxy<RowOperation, Erupt> rowOperationProxy = new RowOperationProxy();

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        switch (invocation.getMethod().getName()) {
            case "filter":
                Filter[] filters = this.rawAnnotation.filter();
                Filter[] proxyFilters = new Filter[filters.length];
                for (int i = 0; i < filters.length; i++) {
                    proxyFilters[i] = AnnotationProxyPool.getOrPut(filters[i], filter ->
                            filterProxy.newProxy(filter, this, this.clazz)
                    );
                }
                return proxyFilters;
            case "rowOperation":
                RowOperation[] rowOperations = this.rawAnnotation.rowOperation();
                List<RowOperation> proxyOperations = new ArrayList<>();
                for (RowOperation rowOperation : rowOperations) {
                    if (ExprInvoke.getExpr(rowOperation.show())) {
                        proxyOperations.add(AnnotationProxyPool.getOrPut(rowOperation, it ->
                                rowOperationProxy.newProxy(it, this, this.clazz)
                        ));
                    }
                }
                return proxyOperations.toArray(new RowOperation[0]);
            case "drills":
                Drill[] drills = this.rawAnnotation.drills();
                Drill[] proxyDrills = new Drill[drills.length];
                for (int i = 0; i < drills.length; i++) {
                    proxyDrills[i] = AnnotationProxyPool.getOrPut(drills[i], it ->
                            drillProxy.newProxy(it, this, this.clazz)
                    );
                }
                return proxyDrills;
        }
        return this.invoke(invocation);
    }

}
