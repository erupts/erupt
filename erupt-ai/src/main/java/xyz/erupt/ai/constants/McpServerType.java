package xyz.erupt.ai.constants;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.Arrays;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/3/24 22:57
 */
public enum McpServerType {

    SSE,
    STDIO,
    ;

    public static class H implements ChoiceFetchHandler {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(McpServerType.values())
                    .map(it -> new VLModel(it.name(), it.name())).toList();
        }
    }

}
