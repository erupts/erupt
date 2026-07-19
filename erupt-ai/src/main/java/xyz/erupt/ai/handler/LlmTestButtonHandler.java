package xyz.erupt.ai.handler;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Service
public class LlmTestButtonHandler implements EruptButtonHandler<LLM> {

    private static final String TEST_MESSAGE = "Hello, please reply with one short sentence.";

    @Resource
    private EruptDao eruptDao;

    @Override
    public String click(LLM llm, String[] params) {
        requireField(llm.getLlm(), "Model Provider");
        requireField(llm.getModel(), "Model");
        requireField(llm.getApiUrl(), "API Domain");
        requireField(llm.getConfig(), "Model Config");
        // In the edit form the frontend sends a placeholder instead of the real API key
        if (null != llm.getId() && (null == llm.getApiKey() || llm.getApiKey().isBlank()
                || EruptConst.PASSWORD_PLACEHOLDER.equals(llm.getApiKey()))) {
            Optional.ofNullable(eruptDao.lambdaQuery(LLM.class).eq(LLM::getId, llm.getId()).one())
                    .ifPresent(it -> llm.setApiKey(it.getApiKey()));
        }
        LlmCore llmCore = LlmCore.getLLM(llm);
        if (null == llmCore) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Unknown model provider: ") + llm.getLlm());
        }
        try {
            ChatModel chatModel = llmCore.buildChatModel(llm.toLlmRequest(), new ArrayList<>());
            String response = chatModel.chat(TEST_MESSAGE);
            return "alert(" + GsonFactory.getGson().toJson(response) + ")";
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
