package xyz.erupt.test.config;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.config.boot.ConfigJdbcReader;
import xyz.erupt.config.form.ConfigCopyForm;
import xyz.erupt.config.handler.ConfigItemHandler;
import xyz.erupt.config.model.ConfigEffectiveValue;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.service.EruptConfig;
import xyz.erupt.config.service.EruptConfigService;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.handler.ConfigRefreshHandler;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.view.TreeModel;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.PreEruptDataService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runtime settings: the layered profiles' items become properties, @Value fields follow a
 * refresh, secrets are masked in the table, and keys are copied between profiles.
 */
class ConfigTest extends EruptApplicationTests {

    private enum Keys {
        GREETING("demo.greeting");

        private final String key;

        Keys(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    @Resource
    private EruptConfigService configService;

    @Resource
    private EruptConfig config;

    @Resource
    private ConfigDemoBean demoBean;

    @Resource
    private ConfigRefreshListener listener;

    @Resource
    private Environment environment;

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private ConfigItemHandler handler;

    @Resource
    private EruptDataController dataController;

    @Resource
    private ConfigRefreshHandler refreshHandler;

    @Resource
    private PreEruptDataService preEruptDataService;

    @Resource
    private PlatformTransactionManager transactionManager;

    private ConfigProfile base;

    private ConfigProfile test;

    private String token;

    @BeforeEach
    void setUp() {
        assertEquals(List.of(ConfigProfile.DEFAULT, "test"), configService.layers(), "default, then spring.profiles.active");
        base = this.profile(ConfigProfile.DEFAULT);
        test = this.profile("test");
        assertNotNull(test, "the running profiles are created at startup");
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                eruptDao.lambdaQuery(ConfigItem.class).list().forEach(eruptDao::delete));
        configService.refresh();
        listener.changes.clear();
        MetaUserinfo user = new MetaUserinfo();
        user.setId(-70L);
        user.setAccount("config-user");
        user.setUsername("config-user");
        token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, List.of(MetaMenu.createEruptClassMenu(ConfigItem.class, null, 1)), token, 5);
        this.pickInTree(test);
    }

    @AfterEach
    void tearDown() {
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    private ConfigProfile profile(String code) {
        return eruptDao.lambdaQuery(ConfigProfile.class).eq(ConfigProfile::getCode, code).list().stream().findFirst().orElseGet(() -> {
            ConfigProfile profile = new ConfigProfile();
            profile.setCode(code);
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> eruptDao.persist(profile));
            return profile;
        });
    }

    // What the page sends once a profile is picked in the tree: the link header an add fills the profile from
    private void pickInTree(ConfigProfile profile) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("link", String.valueOf(profile.getId()));
        request.addHeader("token", token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MetaContext.remove();
        MetaContext.register(new MetaUser(-70L, "config-user", "config-user"));
        MetaContext.registerToken(token);
        MetaContext.register(new MetaErupt(ConfigItem.class.getSimpleName()));
    }

    private ConfigItem item(ConfigProfile profile, String key, String value, boolean secret) {
        ConfigItem item = new ConfigItem();
        item.setEnabled(true);
        item.setProfile(profile);
        item.setKey(key);
        item.setValue(value);
        item.setSecret(secret);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> eruptDao.persist(item));
        return item;
    }

    private JsonObject form(String key, String type) {
        JsonObject form = new JsonObject();
        form.addProperty("key", key);
        form.addProperty("type", type);
        form.addProperty("secret", false);
        form.addProperty("enabled", true);
        return form;
    }

    @Test
    void valuesBecomePropertiesAndReachValueFields() {
        assertEquals("hello", demoBean.getGreeting(), "the file's default until the database says otherwise");
        this.item(base, "demo.greeting", "hi there", false);
        this.item(base, "demo.limit", "5", false);
        Set<String> changed = configService.refresh();
        assertEquals(Set.of("demo.greeting", "demo.limit"), changed);
        assertEquals("hi there", environment.getProperty("demo.greeting"));
        assertEquals("hi there", demoBean.getGreeting(), "the @Value field was set again");
        assertEquals(5, demoBean.getLimit());
        assertEquals("hi there", config.get(Keys.GREETING));
        assertEquals(5, config.get("demo.limit", Integer.class, 0));
        assertEquals(1, listener.changes.size());

        assertTrue(configService.refresh().isEmpty(), "nothing changed: no event");
        assertEquals(1, listener.changes.size());
    }

    @Test
    void theActiveProfileLayersOverDefaultAndOtherProfilesStayOut() {
        this.item(base, "demo.greeting", "shared", false);
        this.item(base, "demo.limit", "3", false);
        this.item(test, "demo.greeting", "for tests", false);
        this.item(this.profile("prod"), "demo.greeting", "for prod", false);
        configService.refresh();
        assertEquals("for tests", environment.getProperty("demo.greeting"), "the active profile wins");
        assertEquals("3", environment.getProperty("demo.limit"), "a key the profile does not override comes from default");
        assertEquals("for tests", demoBean.getGreeting());
        assertTrue(configService.reads("test"));
        assertFalse(configService.reads("prod"));
    }

    @Test
    void theEffectiveValuesPageShowsWhatIsReadAndWhereItCameFrom() {
        this.item(base, "demo.greeting", "shared", false);
        this.item(base, "demo.token", "s3cret", true);
        this.item(test, "demo.greeting", "for tests", false);
        assertEquals("msg.success('2')", refreshHandler.exec(List.of(), null, new String[0]), "the button answers how many keys changed");
        MetaContext.register(new MetaErupt(ConfigEffectiveValue.class.getSimpleName()));
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(100);
        Map<String, Map<String, Object>> rows = new java.util.HashMap<>();
        for (Map<String, Object> row : eruptService.getEruptData(EruptCoreService.getErupt(ConfigEffectiveValue.class.getSimpleName()), query, null).getList()) {
            rows.put(String.valueOf(row.get("key")), row);
        }
        assertEquals("for tests", rows.get("demo.greeting").get("value"));
        assertEquals("test", rows.get("demo.greeting").get("source"), "the overriding layer is named, not default");
        assertTrue(String.valueOf(rows.get("demo.token").get("value")).contains(ConfigItem.MASK), "secrets stay masked on this page too");
        assertEquals(ConfigProfile.DEFAULT, rows.get("demo.token").get("source"));
    }

    @Test
    void theJdbcReaderSeesWhatTheContextWillSeeBeforeItStarts() throws Exception {
        this.item(base, "demo.early", "yes", false);
        this.item(base, "demo.layered", "base", false);
        this.item(test, "demo.layered", "over", false);
        Map<String, Object> values = ConfigJdbcReader.read(environment.getProperty("spring.datasource.url"),
                environment.getProperty("spring.datasource.username"), environment.getProperty("spring.datasource.password"),
                configService.layers(), true);
        assertEquals("yes", values.get("demo.early"));
        assertEquals("over", values.get("demo.layered"), "the reader layers the same way the service does");
    }

    @Test
    void secretsAreMaskedInTheTableAndKeysAreUniquePerProfile() {
        this.item(test, "demo.token", "s3cret", true);
        configService.refresh();
        MetaContext.register(new MetaErupt(ConfigItem.class.getSimpleName()));
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(100);
        query.setLinkTreeVal(List.of(test.getId()));
        Map<String, Object> row = eruptService.getEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), query, null).getList().iterator().next();
        assertTrue(String.valueOf(row.get("value")).contains(ConfigItem.MASK), "the cell shows the mask");
        assertFalse(String.valueOf(row.get("value")).contains("s3cret"));
        assertEquals("s3cret", environment.getProperty("demo.token"), "masked for eyes, not for the application");

        JsonObject duplicate = this.form("demo.token", "STRING");
        duplicate.addProperty("value", "other");
        // refused by the proxy, or by the unique constraint erupt reports first: either way a refusal
        assertThrows(RuntimeException.class, () -> eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), duplicate));
    }

    @Test
    void theTreeBesideTheTableListsTheProfilesByCode() {
        this.profile("prod");
        List<String> labels = dataController.getDependTree(ConfigItem.class.getSimpleName()).stream().map(TreeModel::getLabel).map(String::valueOf).toList();
        assertEquals(ConfigProfile.DEFAULT, labels.get(0), "default comes first");
        assertTrue(labels.containsAll(List.of("test", "prod")));
    }

    @Test
    void anAddTakesItsProfileFromTheTree() {
        JsonObject form = this.form("demo.fromTree", "STRING");
        form.addProperty("value", "1");
        Object id = eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), form);
        assertEquals(test.getId(), eruptDao.find(ConfigItem.class, id).getProfile().getId());
    }

    @Test
    void keysAreCopiedToAnotherProfile() {
        ConfigProfile prod = this.profile("prod");
        ConfigItem item = this.item(test, "demo.copied", "1", false);
        ConfigCopyForm form = new ConfigCopyForm();
        form.setProfile(prod.getId());
        handler.exec(List.of(item), form, new String[0]);
        ConfigItem copy = eruptDao.lambdaQuery(ConfigItem.class).with(ConfigItem::getProfile).eq(ConfigProfile::getId, prod.getId()).with().eq(ConfigItem::getKey, "demo.copied").one();
        assertEquals("1", copy.getValue());
        assertNull(environment.getProperty("demo.copied.other"), "another profile's values stay out of this instance");

        form.setProfile(test.getId());
        assertEquals("msg.success('0')", handler.exec(List.of(item), form, new String[0]), "copying onto itself does nothing");
    }

    @Test
    void theDefaultProfileAndAProfileWithItemsCannotBeDeleted() {
        ConfigProfile prod = this.profile("prod");
        this.item(prod, "demo.keep", "1", false);
        MetaContext.register(new MetaErupt(ConfigProfile.class.getSimpleName()));
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptModifyService.deleteEruptData(EruptCoreService.getErupt(ConfigProfile.class.getSimpleName()), List.of(base.getId()), false));
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptModifyService.deleteEruptData(EruptCoreService.getErupt(ConfigProfile.class.getSimpleName()), List.of(prod.getId()), false));
        assertNotNull(eruptDao.find(ConfigProfile.class, base.getId()));
    }

    @Test
    void theEditorOfTheTypeWritesTheValueAndKeyValuesFlattenToProperties() {
        JsonObject number = this.form("demo.port", "NUMBER");
        number.addProperty("numberValue", 8080);
        MetaContext.register(new MetaErupt(ConfigItem.class.getSimpleName()));
        Object id = eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), number);
        assertEquals("8080", eruptDao.find(ConfigItem.class, id).getValue(), "a whole number is stored without a decimal part");
        assertEquals(8080, config.get("demo.port", Integer.class));

        JsonObject labels = this.form("demo.labels", "KEY_VALUE");
        JsonObject map = new JsonObject();
        map.addProperty("team", "data");
        map.addProperty("tier", "gold");
        labels.add("mapValue", map);
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), labels);
        assertEquals("data", environment.getProperty("demo.labels.team"), "each entry is a property of its own");
        assertEquals("gold", environment.getProperty("demo.labels.tier"));

        JsonObject file = this.form("demo.logo", ConfigValues.ATTACHMENT);
        file.addProperty("fileValue", "/config/logo.png");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), file);
        assertEquals("/config/logo.png", environment.getProperty("demo.logo"), "an attachment's property is its upload path");
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(100);
        query.setLinkTreeVal(List.of(test.getId()));
        Map<String, Object> cell = eruptService.getEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), query, null).getList().stream()
                .filter(row -> "demo.logo".equals(row.get("key"))).findFirst().orElseThrow();
        assertTrue(String.valueOf(cell.get("value")).contains("href='/erupt-attachment/config/logo.png'"), "the cell links to the file");
        assertFalse(String.valueOf(cell.get("value")).contains("<img"), "an attachment is a link, whatever its extension");

        JsonObject image = this.form("demo.banner", ConfigValues.IMAGE);
        image.addProperty("imageValue", "/config/banner.png");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), image);
        Map<String, Object> picture = eruptService.getEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), query, null).getList().stream()
                .filter(row -> "demo.banner".equals(row.get("key"))).findFirst().orElseThrow();
        assertTrue(String.valueOf(picture.get("value")).contains("<img src='/erupt-attachment/config/banner.png'"), "an image shows itself");

        JsonObject icon = this.form("demo.icon", ConfigValues.ICON);
        icon.addProperty("iconValue", "fa fa-bell");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), icon);
        assertEquals("fa fa-bell", environment.getProperty("demo.icon"));

        JsonObject time = this.form("demo.opensAt", ConfigValues.TIME);
        time.addProperty("timeValue", "09:30:00");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), time);
        assertEquals(java.time.LocalTime.of(9, 30), config.get("demo.opensAt", java.time.LocalTime.class), "an ISO time binds to a LocalTime");

        JsonObject bad = this.form("demo.json", "JSON");
        bad.addProperty("jsonValue", "{not json");
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), bad));
    }

    @Test
    void structuredTypesFlattenLikeAFileAndKeepTheRawText() {
        MetaContext.register(new MetaErupt(ConfigItem.class.getSimpleName()));
        JsonObject json = this.form("demo.rules", ConfigValues.JSON);
        json.addProperty("jsonValue", "{\"maxRetries\": 3, \"backoff\": [1, 2, 4], \"nested\": {\"on\": true}}");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), json);
        assertEquals(3, config.get("demo.rules.maxRetries", Integer.class));
        assertEquals("2", environment.getProperty("demo.rules.backoff[1]"), "arrays index the way Spring binds a List");
        assertEquals(true, config.get("demo.rules.nested.on", Boolean.class));
        assertTrue(environment.getProperty("demo.rules").startsWith("{"), "the raw JSON stays under the key");

        JsonObject yaml = this.form("demo.mail", ConfigValues.YAML);
        yaml.addProperty("yamlValue", "host: smtp.example.com\nport: 587\nto:\n  - a@example.com\n  - b@example.com\n");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), yaml);
        assertEquals("smtp.example.com", environment.getProperty("demo.mail.host"));
        assertEquals(587, config.get("demo.mail.port", Integer.class));
        assertEquals("b@example.com", environment.getProperty("demo.mail.to[1]"));

        JsonObject badYaml = this.form("demo.badYaml", ConfigValues.YAML);
        badYaml.addProperty("yamlValue", "a: [unclosed");
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), badYaml));

        JsonObject list = this.form("demo.origins", ConfigValues.LIST);
        list.addProperty("listValue", "a.com,b.com");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), list);
        assertEquals(List.of("a.com", "b.com"), config.get("demo.origins", List.class), "a comma list binds to a List");

        JsonObject date = this.form("demo.since", ConfigValues.DATETIME);
        date.addProperty("dateTimeValue", "2026-10-20 09:30:00");
        eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), date);
        assertEquals(java.time.LocalDateTime.of(2026, 10, 20, 9, 30), config.get("demo.since", java.time.LocalDateTime.class), "an ISO date-time binds to a LocalDateTime");
    }

    @Test
    void aDisabledItemStaysInTheTableAndLeavesTheApplication() throws Exception {
        ConfigItem item = this.item(base, "demo.switch", "on", false);
        configService.refresh();
        assertEquals("on", environment.getProperty("demo.switch"));

        item.setEnabled(false);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> eruptDao.merge(item));
        assertEquals(Set.of("demo.switch"), configService.refresh(), "switching off is a change");
        assertNull(environment.getProperty("demo.switch"), "the files' value applies again");
        assertNotNull(eruptDao.find(ConfigItem.class, item.getId()), "the row is kept");
        Map<String, Object> early = ConfigJdbcReader.read(environment.getProperty("spring.datasource.url"),
                environment.getProperty("spring.datasource.username"), environment.getProperty("spring.datasource.password"), configService.layers(), true);
        assertFalse(early.containsKey("demo.switch"), "the startup reader skips it too");
    }

    @Test
    void aPasswordIsAlwaysASecretAndTheFormNeverSeesIt() {
        MetaContext.register(new MetaErupt(ConfigItem.class.getSimpleName()));
        JsonObject password = this.form("demo.password", ConfigValues.PASSWORD);
        password.addProperty("passwordValue", "p@ss");
        Object id = eruptModifyService.insertEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), password);
        ConfigItem stored = eruptDao.find(ConfigItem.class, id);
        assertTrue(stored.getSecret(), "the type implies the flag");
        assertEquals("p@ss", environment.getProperty("demo.password"));

        Map<String, Object> form = preEruptDataService.getEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), String.valueOf(id), false);
        assertEquals(ConfigItem.MASK, form.get("passwordValue"), "the edit form gets the mask, not the value");
        assertEquals(ConfigItem.MASK, form.get("value"));

        JsonObject unchanged = new JsonObject();
        unchanged.addProperty("id", String.valueOf(id));
        unchanged.addProperty("key", "demo.password");
        unchanged.addProperty("type", ConfigValues.PASSWORD);
        unchanged.addProperty("passwordValue", ConfigItem.MASK);
        unchanged.addProperty("secret", true);
        unchanged.addProperty("enabled", true);
        unchanged.addProperty("remark", "touched");
        eruptModifyService.updateEruptData(EruptCoreService.getErupt(ConfigItem.class.getSimpleName()), unchanged);
        assertEquals("p@ss", eruptDao.find(ConfigItem.class, id).getValue(), "the mask posted back keeps the stored value");
    }

}
