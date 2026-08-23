package xyz.erupt.ai_canvas.proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.jpa.dao.EruptDao;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Component
public class AiCanvasDataProxy implements DataProxy<AiCanvas> {

    // Frontend viewer route, used as the ROUTER menu value by AiCanvasMenuHandler
    public static final String ROUTE_PATH = "/ai/canvas/";

    // Chrome-less full-screen variant, opened in the Path column's preview dialog
    public static final String FILL_PATH = "#/fill" + ROUTE_PATH;

    // Unambiguous alphabet (no 0/o/1/l); 6 chars ≈ 1 billion combinations
    private static final char[] CODE_CHARS = "abcdefghijkmnpqrstuvwxyz23456789".toCharArray();

    private static final int CODE_LENGTH = 6;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(AiCanvas aiCanvas) {
        if (StringUtils.isBlank(aiCanvas.getCode())) {
            aiCanvas.setCode(this.generateCode());
        }
    }

    @Override
    public void beforeUpdate(AiCanvas aiCanvas) {
        this.beforeAdd(aiCanvas);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        list.forEach(row -> row.put("path", FILL_PATH + row.get("code")));
    }

    private String generateCode() {
        while (true) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)]);
            }
            if (eruptDao.lambdaQuery(AiCanvas.class).eq(AiCanvas::getCode, code.toString()).count() == 0) {
                return code.toString();
            }
        }
    }

}
