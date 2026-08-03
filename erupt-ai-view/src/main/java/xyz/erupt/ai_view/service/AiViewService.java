package xyz.erupt.ai_view.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.ai_view.model.AiView;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds the generation prompt (API skill + target model structures + user
 * requirement), sends it to the LLM and stores the returned HTML page.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
public class AiViewService {

    private static final String SKILL_PATH = "/prompts/ai-view-skill.md";

    private static final String HTML_FENCE = "```html";

    private static String skillPrompt;

    @Resource
    private LLMService llmService;

    @Resource
    private EruptDao eruptDao;

    @Transactional
    public void generate(AiView view) {
        StringBuilder system = new StringBuilder(this.skill());
        system.append("\n\n# Available Erupt Models\n");
        for (String eruptName : view.getTargetErupts().split(",")) {
            this.describeModel(eruptName.trim(), system);
        }
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(system.toString()));
        messages.add(UserMessage.from(this.userMessage(view)));
        String response = null == view.getLlm() ? llmService.send(messages) : llmService.send(view.getLlm(), messages);
        view.setHtml(this.extractHtml(response));
        eruptDao.merge(view);
    }

    private String userMessage(AiView view) {
        StringBuilder user = new StringBuilder(view.getRequirement());
        if (StringUtils.isNotBlank(view.getHtml())) {
            user.append("\n\n# Current Page Source\nRevise the page below against the requirement above and output the full document again.\n")
                    .append(HTML_FENCE).append("\n").append(view.getHtml()).append("\n```");
        }
        return user.toString();
    }

    private void describeModel(String eruptName, StringBuilder sb) {
        EruptModel model = EruptCoreService.getErupt(eruptName);
        if (null == model) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-view.model_not_found") + ": " + eruptName);
        }
        sb.append("\n## ").append(model.getEruptName()).append("\n");
        sb.append("Primary key field: `id`\n\n");
        sb.append("```json\n").append(GsonFactory.getGson().toJson(model)).append("\n```\n");
    }

    private String extractHtml(String response) {
        if (StringUtils.isBlank(response)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-view.empty_response"));
        }
        int fence = response.indexOf(HTML_FENCE);
        if (fence >= 0) {
            int contentStart = fence + HTML_FENCE.length();
            int end = response.lastIndexOf("```");
            if (end > contentStart) return response.substring(contentStart, end).trim();
        }
        // Fallback: the model answered with a bare document, no code fence
        int docStart = response.indexOf("<!DOCTYPE");
        if (docStart < 0) docStart = response.indexOf("<html");
        int docEnd = response.lastIndexOf("</html>");
        if (docStart >= 0 && docEnd > docStart) {
            return response.substring(docStart, docEnd + "</html>".length());
        }
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai-view.bad_response"));
    }

    @SneakyThrows
    private String skill() {
        if (null == skillPrompt) {
            try (InputStream is = AiViewService.class.getResourceAsStream(SKILL_PATH)) {
                skillPrompt = new String(Objects.requireNonNull(is, SKILL_PATH).readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return skillPrompt;
    }

}
