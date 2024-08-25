package xyz.erupt.bi.constant;

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

    STRING("string", "文本", null),
    NUMBER("number", "数值", "数值靠右显示"),
    DATE("date", "时间", "时间居中显示"),
    LONG_TEXT("long_text", "长文本", "适用于长文本场景，会自动折叠"),
    PERCENT("percent", "百分比", "进度条方式展示百分比"),
    LINK("link", "链接", null),
    LINK_DIALOG("link_dialog", "对话框方式打开链接", null),
    DRILL("drill", "下钻", null),
    ;

    private final String code;

    private final String title;

    private final String desc;

    public static class Fetch implements ChoiceFetchHandler {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(ColumnType.values()).map(it -> new VLModel(it.code, it.getTitle(), it.getDesc())).collect(Collectors.toList());
        }
    }

}
