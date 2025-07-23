package xyz.erupt.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2025/2/25 22:19
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai")
public class AiProp {

    private String systemPrompt = "你是 Erupt AI，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Erupt AI 为专有名词，不可翻译成其他语言。";

    private boolean devMode = false;

    private String apiDomain;

    private String nameForHuman;

    // 用户界面中显示的服务简介
    private String descriptionForHuman;

    // 向模型描述服务用途
    private String descriptionForModel;

    //联系邮箱
    private String contactEmail;

    //法律声明页面链接
    private String legalInfoUrl;

}
