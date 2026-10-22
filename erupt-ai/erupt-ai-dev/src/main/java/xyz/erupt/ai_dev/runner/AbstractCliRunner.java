package xyz.erupt.ai_dev.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;

import java.util.ArrayList;
import java.util.List;

/**
 * A built-in runner: its binary, label and extra arguments can be overridden under
 * {@code erupt.ai-dev.executor.clis.<code>}.
 *
 * @author YuePeng
 * date 2026/10/23
 */
public abstract class AbstractCliRunner implements CodingAgentRunner {

    @Resource
    protected EruptAiDevProp eruptAiDevProp;

    protected abstract String defaultBinary();

    protected abstract String defaultLabel();

    // granted tools and the like; headless runs cannot ask
    protected List<String> defaultArgs() {
        return List.of();
    }

    private EruptAiDevProp.Cli cli() {
        EruptAiDevProp.Cli cli = eruptAiDevProp.getExecutor().getClis().get(this.code());
        return null == cli ? new EruptAiDevProp.Cli() : cli;
    }

    @Override
    public String binary() {
        return StringUtils.defaultIfBlank(this.cli().getBinary(), this.defaultBinary());
    }

    @Override
    public String label() {
        return StringUtils.defaultIfBlank(this.cli().getLabel(), this.defaultLabel());
    }

    protected List<String> args() {
        return new ArrayList<>(null == this.cli().getArgs() ? this.defaultArgs() : this.cli().getArgs());
    }

    protected static String text(JsonObject obj, String key) {
        JsonElement value = null == obj ? null : obj.get(key);
        return null == value || value.isJsonNull() ? "" : value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }

    protected static JsonObject object(JsonObject obj, String key) {
        JsonElement value = null == obj ? null : obj.get(key);
        return null != value && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    protected static Long number(JsonObject obj, String... keys) {
        if (null == obj) return null;
        long total = 0;
        boolean any = false;
        for (String key : keys) {
            JsonElement value = obj.get(key);
            if (null != value && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                total += value.getAsLong();
                any = true;
            }
        }
        return any ? total : null;
    }

}
