package xyz.erupt.report.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2022/5/16 21:18
 */
@Getter
@AllArgsConstructor
public enum ColumnType {

    STRING("string", "Text", null),
    NUMBER("number", "Number", "right-aligned"),
    DATE("date", "Time", "center-aligned"),
    LONG_TEXT("long_text", "Long Text", "auto-collapsed for long text"),
    PERCENT("percent", "Percent", "displayed as a progress bar"),
    LINK("link", "Link", null),
    LINK_DIALOG("link_dialog", "Link (Dialog)", null),
    DRILL("drill", "Drill Down", null),
    ;

    private final String code;

    private final String title;

    private final String desc;

    public static class Fetch implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(ColumnType.values()).map(it -> new VLModel(it.code, it.getTitle(), it.getDesc())).collect(Collectors.toList());
        }
    }

}
