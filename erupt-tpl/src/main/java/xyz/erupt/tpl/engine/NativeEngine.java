package xyz.erupt.tpl.engine;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;
import xyz.erupt.annotation.sub_erupt.Tpl;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/1/29 17:10
 */
public class NativeEngine extends EngineTemplate<Void> {

    public static final String baseDir = "${base}";

    @Override
    public Tpl.Engine engine() {
        return Tpl.Engine.Native;
    }

    @Override
    public Void init() {
        return null;
    }

    @SneakyThrows
    @Override
    public void render(Void unused, String filePath, Map<String, Object> bindingMap, Writer out) {
        @Cleanup InputStream inputStream = this.getClass().getResourceAsStream(filePath);
        String html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        HttpServletRequest request = (HttpServletRequest) bindingMap.get(EngineConst.INJECT_REQUEST);
        out.write(html.replace(baseDir, request.getContextPath()));
    }
}
