package xyz.erupt.test.filecenter;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.filecenter.EruptFileCenterAutoConfiguration;
import xyz.erupt.filecenter.store.FileEntry;
import xyz.erupt.filecenter.store.FileStore;
import xyz.erupt.filecenter.store.FileStores;
import xyz.erupt.filecenter.web.FileCenterController;
import xyz.erupt.filecenter.web.FileCenterTpl;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.tpl.service.EruptTplService;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.upms.service.EruptTokenService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The file center over the upload directory: listing, writing, the traversal guard, and the
 * write button a change needs.
 */
class FileCenterTest extends EruptApplicationTests {

    @Resource
    private FileStores stores;

    @Resource
    private FileCenterController controller;

    @Resource
    private FileCenterTpl tpl;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptTplService tplService;

    private String viewer;

    private String writer;

    @BeforeEach
    void setUp() throws Exception {
        MetaMenu page = MetaMenu.createSimpleMenu("file-center", "File Center", FileCenterTpl.PAGE, null, 1, "tpl");
        MetaMenu write = MetaMenu.createSimpleMenu("file-center-write", "Write", FileCenterTpl.WRITE, page, 1, "button");
        viewer = this.login(-81L, List.of(page));
        writer = this.login(-82L, List.of(page, write));
        this.as(writer);
        stores.get(FileStores.UPLOADS).delete("/file-center-test");
    }

    @AfterEach
    void tearDown() {
        MetaContext.remove();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    private String login(long id, List<MetaMenu> menus) {
        MetaUserinfo user = new MetaUserinfo();
        user.setId(id);
        user.setAccount("file-user" + id);
        user.setUsername("file-user" + id);
        String token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, menus, token, 5);
        return token;
    }

    private void as(String token) {
        MetaContext.remove();
        MetaContext.register(new MetaUser(-80L, "file-user", "file-user"));
        MetaContext.registerToken(token);
    }

    private MultipartFile file(String name, String content) {
        return new MockMultipartFile("files", name, "text/plain", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void theUploadDirectoryIsTheFirstStoreAndIsBrowsedInColumns() throws Exception {
        assertEquals(FileStores.UPLOADS, stores.names().get(0));
        controller.mkdir(FileStores.UPLOADS, "/", "file-center-test");
        controller.mkdir(FileStores.UPLOADS, "/file-center-test", "docs");
        controller.upload(FileStores.UPLOADS, "/file-center-test", new MultipartFile[]{this.file("readme.txt", "hello"), this.file("b.csv", "1,2")});

        List<FileEntry> entries = controller.list(FileStores.UPLOADS, "/file-center-test").getData();
        assertEquals(List.of("docs", "b.csv", "readme.txt"), entries.stream().map(FileEntry::name).toList(), "folders first, then files by name");
        FileEntry readme = entries.get(2);
        assertEquals("/file-center-test/readme.txt", readme.path());
        assertEquals(5, readme.size());
        assertEquals("text/plain", readme.contentType());

        Map<String, Object> stat = controller.stat(FileStores.UPLOADS, readme.path()).getData();
        assertEquals("/erupt-attachment/file-center-test/readme.txt", stat.get("url"), "uploads are served by erupt already");

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.raw(FileStores.UPLOADS, readme.path(), null, response);
        assertEquals("hello", response.getContentAsString());
        assertTrue(response.getHeader("Content-Disposition").startsWith("inline"));

        controller.rename(FileStores.UPLOADS, readme.path(), "README.md");
        controller.delete(FileStores.UPLOADS, List.of("/file-center-test/docs"));
        assertEquals(List.of("b.csv", "README.md"), controller.list(FileStores.UPLOADS, "/file-center-test").getData().stream().map(FileEntry::name).toList());
    }

    @Test
    void nothingClimbsOutOfAStore() {
        assertThrows(EruptWebApiRuntimeException.class, () -> controller.list(FileStores.UPLOADS, "/../.."));
        assertThrows(EruptWebApiRuntimeException.class, () -> controller.mkdir(FileStores.UPLOADS, "/", "../escape"));
        assertThrows(IllegalArgumentException.class, () -> FileStore.join("/a", "b/c"));
        assertEquals("/a/b", FileStore.normalize("a//b/./"));
    }

    @Test
    void aChangeNeedsTheWriteButton() throws Exception {
        this.as(viewer);
        assertFalse((Boolean) tpl.page().get("writable"));
        assertNotNull(controller.list(FileStores.UPLOADS, "/").getData(), "reading is the page's menu");
        assertThrows(EruptWebApiRuntimeException.class, () -> controller.mkdir(FileStores.UPLOADS, "/", "file-center-test"));
        assertThrows(EruptWebApiRuntimeException.class, () -> controller.delete(FileStores.UPLOADS, List.of("/file-center-test")));

        this.as(writer);
        assertTrue((Boolean) tpl.page().get("writable"));
        assertTrue(new EruptFileCenterAutoConfiguration().initMenus().stream().anyMatch(it -> FileCenterTpl.WRITE.equals(it.getValue())));
    }

    @Test
    void thePageRendersWithItsStoresAndToken() throws Exception {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("token", writer);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request, new MockHttpServletResponse()));
        java.io.StringWriter out = new java.io.StringWriter();
        tplService.tplRender(Tpl.Engine.FreeMarker, "tpl/" + FileCenterTpl.PAGE, tpl.page(), out);
        String html = out.toString();
        assertTrue(html.contains("const STORES = ['Uploads']"), html.substring(0, Math.min(html.length(), 400)));
        assertTrue(html.contains("TOKEN = '" + writer + "'"));
        assertTrue(html.contains("WRITABLE = true"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/file-center.rendered.html"), html);
    }

}
