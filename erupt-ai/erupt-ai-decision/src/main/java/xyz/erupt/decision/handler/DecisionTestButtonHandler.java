package xyz.erupt.decision.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.question.Noul;
import xyz.erupt.decision.question.Question;
import xyz.erupt.decision.service.DecisionService;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Map;
import java.util.Optional;

/**
 * Verifies the endpoint and the key by putting the cheapest possible question to the provider.
 *
 * @author YuePeng
 */
@Service
public class DecisionTestButtonHandler implements EruptButtonHandler<DecisionModel> {

    private static final String TEST_ID = "test";

    private static final String TEST_STATE = "The quick brown fox jumps over the lazy dog.";

    private static final Noul TEST_QUESTION = Noul.of("Is this sentence written in English?");

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DecisionService decisionService;

    @Override
    public String click(DecisionModel config, String[] params) {
        this.requireField(config.getProvider(), "Provider");
        this.requireField(config.getModel(), "Model");
        this.requireField(config.getApiUrl(), "API Domain");
        // In the edit form the frontend sends a placeholder instead of the real API key
        if (null != config.getId() && (StringUtils.isBlank(config.getApiKey())
                || EruptConst.PASSWORD_PLACEHOLDER.equals(config.getApiKey()))) {
            Optional.ofNullable(eruptDao.find(DecisionModel.class, config.getId()))
                    .ifPresent(it -> config.setApiKey(it.getApiKey()));
        }
        Decision decision = decisionService.evaluate(config, TEST_STATE, Map.of(TEST_ID, (Question<?>) TEST_QUESTION));
        String message = decision.model() + " → " + decision.noul(TEST_ID).value();
        return "alert(" + GsonFactory.getGson().toJson(message) + ")";
    }

    private void requireField(String value, String title) {
        if (StringUtils.isBlank(value)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(title) + " " + I18nTranslate.$translate("erupt.notnull"));
        }
    }

}
