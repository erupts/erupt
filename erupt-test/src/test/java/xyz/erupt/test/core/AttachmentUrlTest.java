package xyz.erupt.test.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A stored attachment path may only address the configured attachment domain,
 * never a host of its own choosing.
 */
public class AttachmentUrlTest {

    private static final AttachmentProxy PROXY = new AttachmentProxy() {
        @Override
        public String fileDomain() {
            return "https://cdn.erupt.xyz";
        }

        @Override
        public String upLoad(java.io.InputStream inputStream, String path) {
            return path;
        }
    };

    @Test
    public void keepsTheConfiguredHost() {
        assertEquals("https://cdn.erupt.xyz/upload/a.png",
                EruptUtil.attachmentUrl(PROXY, "/upload/a.png").toString());
    }

    @Test
    public void refusesAnotherHost() {
        // each of these moves the resolved host off the configured domain
        for (String path : new String[]{
                "@evil.com/a.png",
                ":pw@evil.com/a.png",
                ".evil.com/a.png"}) {
            assertThrows(EruptWebApiRuntimeException.class, () -> EruptUtil.attachmentUrl(PROXY, path), path);
        }
    }

}
