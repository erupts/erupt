package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class TestDataProxy implements DataProxy<DataProxyModel> {

    @Override
    public void validate(DataProxyModel model) throws EruptException {
        if (model.getTitle() == null || model.getTitle().isBlank()) {
            throw new EruptException("Title cannot be empty");
        }
    }

    @Override
    public void beforeAdd(DataProxyModel model) {
        model.setStatus("DRAFT");
        model.setCreator("system");
    }

    @Override
    public void afterAdd(DataProxyModel model) {
        // business logic triggered after add, e.g. notifications
    }

    @Override
    public void beforeUpdate(DataProxyModel model) {
        // validate status before update
    }

    @Override
    public void afterUpdate(DataProxyModel model) {
        // sync cache or other side-effects after update
    }

    @Override
    public void beforeDelete(DataProxyModel model) {
        if ("PUBLISHED".equals(model.getStatus())) {
            throw new EruptException("Published records cannot be deleted");
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        // append extra query conditions
        return null;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        // post-process results, e.g. mask sensitive fields
    }
}
