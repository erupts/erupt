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
        // 新增后触发通知等业务逻辑
    }

    @Override
    public void beforeUpdate(DataProxyModel model) {
        // 修改前校验状态
    }

    @Override
    public void afterUpdate(DataProxyModel model) {
        // 修改后同步缓存等
    }

    @Override
    public void beforeDelete(DataProxyModel model) {
        if ("PUBLISHED".equals(model.getStatus())) {
            throw new EruptException("Published records cannot be deleted");
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        // 附加额外查询条件
        return null;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        // 对查询结果做脱敏等后处理
    }
}
