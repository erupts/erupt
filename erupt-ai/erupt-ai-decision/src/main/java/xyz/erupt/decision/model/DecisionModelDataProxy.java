package xyz.erupt.decision.model;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.decision.core.DecisionCore;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 */
@Component
public class DecisionModelDataProxy implements DataProxy<DecisionModel>, OnChange<DecisionModel>,
        OperationHandler<DecisionModel, Void> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(DecisionModel model) {
        // The first one configured is the one everything falls back to
        model.setDefaultModel(eruptDao.lambdaQuery(DecisionModel.class).count() == 0);
        if (null == model.getSort()) {
            Integer max = (Integer) eruptDao.lambdaQuery(DecisionModel.class).max(DecisionModel::getSort);
            model.setSort(null == max ? 10 : max + 10);
        }
    }

    @Override
    @Transactional
    public String exec(List<DecisionModel> data, Void unused, String[] param) {
        for (DecisionModel model : eruptDao.lambdaQuery(DecisionModel.class).eq(DecisionModel::getDefaultModel, true).list()) {
            model.setDefaultModel(false);
            eruptDao.merge(model);
        }
        eruptDao.find(DecisionModel.class, data.get(0).getId()).setDefaultModel(true);
        return "";
    }

    /** Picking a provider fills in the endpoint and model it ships with */
    @Override
    public Map<String, Object> populateForm(DecisionModel model, String[] params) {
        DecisionCore core = null == model.getProvider() ? null : DecisionCore.get(model.getProvider());
        if (null == core) return Map.of();
        Map<String, Object> form = new HashMap<>();
        form.put(LambdaSee.field(DecisionModel::getApiUrl), core.api());
        form.put(LambdaSee.field(DecisionModel::getModel), core.model());
        return form;
    }

    @Override
    public Map<String, String> buildEditExpr(DecisionModel model, String[] params) {
        return Map.of();
    }

}
