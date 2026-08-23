package xyz.erupt.ai.handler;

import dev.langchain4j.data.embedding.Embedding;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Optional;

/**
 * Embeds a probe sentence and verifies the returned vector size against the
 * configured dimension, so collection-size mismatches surface before ingestion.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Service
public class EmbeddingTestButtonHandler implements EruptButtonHandler<EmbeddingLLM> {

    private static final String TEST_TEXT = "Hello, embedding!";

    @Resource
    private EruptDao eruptDao;

    @Override
    public String click(EmbeddingLLM config, String[] params) {
        requireField(config.getProvider(), "Provider");
        requireField(config.getModel(), "Model");
        if (null == config.getDimension()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Dimension") + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        // In the edit form the frontend sends a placeholder instead of the real API key
        if (null != config.getId() && (null == config.getApiKey() || config.getApiKey().isBlank()
                || EruptConst.PASSWORD_PLACEHOLDER.equals(config.getApiKey()))) {
            Optional.ofNullable(eruptDao.lambdaQuery(EmbeddingLLM.class).eq(EmbeddingLLM::getId, config.getId()).one())
                    .ifPresent(it -> config.setApiKey(it.getApiKey()));
        }
        EmbeddingCore core = EmbeddingCore.get(config.getProvider());
        if (null == core) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Unknown model provider: ") + config.getProvider());
        }
        try {
            Embedding embedding = core.build(config).embed(TEST_TEXT).content();
            if (embedding.dimension() != config.getDimension()) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Embedding dimension mismatch: ")
                        + embedding.dimension() + " ≠ " + config.getDimension());
            }
            return "alert(" + GsonFactory.getGson().toJson(I18nTranslate.$translate("Embedding OK")
                    + " dimension = " + embedding.dimension()) + ")";
        } catch (EruptWebApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    private void requireField(String value, String title) {
        if (null == value || value.isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(title) + " " + I18nTranslate.$translate("erupt.notnull"));
        }
    }

}
