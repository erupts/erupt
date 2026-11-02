package xyz.erupt.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetaContextTest {

    @AfterEach
    void tearDown() {
        MetaContext.remove();
    }

    @Test
    void childThreadInheritsCopyNotSharedInstance() throws InterruptedException {
        MetaContext.register(new MetaErupt("Order", "Order"));
        MetaContext.register(new MetaUser(1L, "admin", "Admin", "t1"));
        MetaContext.registerToken("tk");
        MetaContext.registerLang("zh-CN");
        MetaContext.registerVar("k", "v");
        MetaContext parent = MetaContext.get();

        AtomicReference<MetaContext> inherited = new AtomicReference<>();
        AtomicReference<String> inheritedErupt = new AtomicReference<>();
        AtomicReference<Long> inheritedUid = new AtomicReference<>();
        AtomicReference<String> inheritedToken = new AtomicReference<>();
        AtomicReference<String> inheritedLang = new AtomicReference<>();
        Thread child = new Thread(() -> {
            inherited.set(MetaContext.get());
            inheritedErupt.set(MetaContext.getErupt().getName());
            inheritedUid.set(MetaContext.getUser().getUid());
            inheritedToken.set(MetaContext.getToken());
            inheritedLang.set(MetaContext.getLang());
            // what the child registers must stay the child's
            MetaContext.register(new MetaErupt("NoSuchErupt", "NoSuchErupt"));
            MetaContext.getUser().setTenantId("t2");
            MetaContext.registerVar("k", "changed");
        });
        child.start();
        child.join();

        assertNotSame(parent, inherited.get());
        assertEquals("Order", inheritedErupt.get());
        assertEquals(1L, inheritedUid.get());
        assertEquals("tk", inheritedToken.get());
        assertEquals("zh-CN", inheritedLang.get());

        assertEquals("Order", MetaContext.getErupt().getName());
        assertEquals("t1", MetaContext.getUser().getTenantId());
        assertEquals("v", MetaContext.getVars().get("k"));
    }

    @Test
    void threadWithoutParentContextStartsEmpty() throws InterruptedException {
        AtomicReference<String> name = new AtomicReference<>("unset");
        Thread child = new Thread(() -> name.set(MetaContext.getErupt().getName()));
        child.start();
        child.join();
        assertNull(name.get());
    }
}
