package xyz.erupt.decision.model;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
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
    public void afterAdd(DecisionModel model) {
        this.settleDefault();
    }

    @Override
    public void afterUpdate(DecisionModel model) {
        this.settleDefault();
    }

    @Override
    public void afterDelete(DecisionModel model) {
        this.settleDefault();
    }

    @Override
    @Transactional
    public String exec(List<DecisionModel> data, Void unused, String[] param) {
        DecisionModel picked = eruptDao.find(DecisionModel.class, data.get(0).getId());
        // A locked row would carry the flag while defaultModel() still refused to hand it out
        if (!Boolean.TRUE.equals(picked.getEnable())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("decision.default_model_locked"));
        }
        for (DecisionModel model : eruptDao.lambdaQuery(DecisionModel.class).eq(DecisionModel::getDefaultModel, true).list()) {
            model.setDefaultModel(false);
            eruptDao.merge(model);
        }
        picked.setDefaultModel(true);
        return "";
    }

    /**
     * Keeps the flag on exactly one enabled row. Locking or deleting the row that held it used to
     * leave none, and a decision was then answered by whichever model the fallback query returned
     * first, so the next enabled row by sort takes over and a locked row gives the flag up.
     */
    @Transactional
    public void settleDefault() {
        List<DecisionModel> flagged = eruptDao.lambdaQuery(DecisionModel.class)
                .eq(DecisionModel::getDefaultModel, true).list();
        flagged.stream().filter(it -> !Boolean.TRUE.equals(it.getEnable())).forEach(it -> {
            it.setDefaultModel(false);
            eruptDao.merge(it);
        });
        if (flagged.stream().anyMatch(it -> Boolean.TRUE.equals(it.getEnable()))) return;
        DecisionModel next = eruptDao.lambdaQuery(DecisionModel.class).eq(DecisionModel::getEnable, true)
                .orderByAsc(DecisionModel::getSort).limit(1).one();
        // Nothing enabled is left to promote: defaultModel() then says so rather than guessing
        if (null != next) {
            next.setDefaultModel(true);
            eruptDao.merge(next);
        }
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
