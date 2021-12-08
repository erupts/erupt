package xyz.erupt.bi.handler;

import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.bi.model.Bi;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/12/9 00:17
 */
public class CopyLinkHandler implements OperationHandler<Bi, Void> {

    @Override
    public String exec(List<Bi> data, Void unused, String[] param) {
        Bi bi = data.get(0);
        String link = bi.getCode();
        return "var textArea = document.createElement('textarea');\n" +
                "textArea.value = '" + link + "';\n" +
                "document.body.appendChild(textArea);\n" +
                "textArea.select();\n" +
                "this.msg.success(document.execCommand('Copy') ? '已复制链接到剪切板' : '链接复制失败');\n" +
                "document.body.removeChild(textArea);";
    }

}
