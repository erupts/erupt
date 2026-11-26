package xyz.erupt.test.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.ai.service.LLMRoleService;
import xyz.erupt.ai_claw.prompt.ClawSystemPrompt;
import xyz.erupt.ai_claw.tool.EruptModelTools;
import xyz.erupt.ai_claw.util.ClawSandbox;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.test.model.erupt.RowOperationModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.tenant.UpmsTenantSessions;
import xyz.erupt.upms.helper.UpmsSecurityHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A tenant's administrator is a super admin inside its tenant and nowhere else: through the AI it reads
 * everything its package's menus offer, and nothing of the platform — no model outside the package, no tool
 * that acts on what the whole deployment shares, no file of a platform account that happens to share its name.
 */
class TenantAiAccessTest extends EruptApplicationTests {

    private static final String TENANT = "t-ai-access";

    private static final String ORDERS = OrderByModel.class.getSimpleName();

    private static final String OPERATIONS = RowOperationModel.class.getSimpleName();

    @Resource
    private UpmsTenantSessions sessions;

    @Resource
    private LLMRoleService llmRoleService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptModelTools modelTools;

    @Resource
    private ClawSystemPrompt clawSystemPrompt;

    private Long tenantAdminId;

    private Long platformAdminId;

    @BeforeEach
    void setUp() {
        MetaContext.remove();
        platformAdminId = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one().getId();
        // the tenant's administrator carries the very account name of the platform's
        tenantAdminId = sessions.callAs(TENANT, em -> {
            EruptUser user = new EruptUser();
            user.setAccount(eruptUpmsProp.getDefaultAccount());
            user.setName("Tenant Admin");
            user.setStatus(true);
            user.setIsAdmin(true);
            user.setEncrypt(true);
            UpmsSecurityHelper.applyPassword(user, "secret", true);
            em.persist(user);
            em.flush();
            return user.getId();
        });
    }

    @AfterEach
    void cleanUp() throws IOException {
        MetaContext.remove();
        sessions.runAs(TENANT, em -> {
            EruptUser user = em.find(EruptUser.class, tenantAdminId);
            if (null != user) em.remove(user);
        });
        Path tenantHome = Path.of(System.getProperty("user.home"), ".erupt", ".tenants", TENANT);
        if (Files.exists(tenantHome)) {
            try (Stream<Path> walk = Files.walk(tenantHome)) {
                for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    @Test
    void aTenantsAdministratorIsGrantedTheToolsOfItsTenantButNoneOfThePlatforms() {
        this.asTenantAdmin(List.of());
        Set<String> tenantTools = llmRoleService.getAllowedToolsByUid(tenantAdminId);
        assertTrue(tenantTools.containsAll(List.of("eruptDataQuery", "sendNotice", "listRecordComments")), tenantTools.toString());
        for (String platform : List.of("getProperty", "execShell", "listSkills", "writeSkillFile")) {
            assertFalse(tenantTools.contains(platform), platform + " acts on the deployment, not on the tenant");
        }

        MetaContext.remove();
        assertTrue(llmRoleService.getAllowedToolsByUid(platformAdminId).containsAll(List.of("getProperty", "listSkills")),
                "the platform's administrator keeps them");
    }

    @Test
    void aTenantsAdministratorReachesTheModelsOfItsPackageOnly() {
        this.asTenantAdmin(List.of(MetaMenu.createEruptClassMenu(OrderByModel.class, null, 1)));
        String listed = modelTools.eruptModelList();
        assertTrue(listed.contains(ORDERS + ":"), listed);
        assertFalse(listed.contains(OPERATIONS + ":"), "a model outside the package is not offered");
        assertFalse(listed.contains("EruptTenantConfig") || listed.contains("LLM:"), listed);
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.eruptSchema(OPERATIONS));
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.eruptOperations(OPERATIONS));

        // the platform's administrator still skips the menus
        this.login(platformAdminId, null, List.of());
        assertTrue(modelTools.eruptModelList().contains(OPERATIONS + ":"));
    }

    @Test
    void aTenantsAdministratorIsNotPromptedTowardsThePlatformsTools() {
        this.asTenantAdmin(List.of());
        String tenant = clawSystemPrompt.getPrompt();
        assertTrue(tenant.contains("eruptSchema"), "the data tools it holds are still explained");
        for (String platform : List.of("## Installed Skills", "listSkills", "execShell", "saveSkill")) {
            assertFalse(tenant.contains(platform), platform + " is the platform's");
        }

        this.login(platformAdminId, null, List.of());
        String platform = clawSystemPrompt.getPrompt();
        assertTrue(platform.contains("## Installed Skills") && platform.contains("execShell"), platform);
    }

    @Test
    void aTenantsUserNeverSharesASandboxWithThePlatformsAccountOfTheSameName() {
        this.login(platformAdminId, null, List.of());
        Path platform = ClawSandbox.root();
        this.asTenantAdmin(List.of());
        Path tenant = ClawSandbox.root();
        assertNotEquals(platform, tenant);
        assertFalse(tenant.startsWith(platform) || platform.startsWith(tenant), "neither contains the other");
        assertTrue(tenant.toString().contains(TENANT), tenant.toString());
    }

    private void asTenantAdmin(List<MetaMenu> menus) {
        this.login(tenantAdminId, TENANT, menus);
    }

    private void login(Long uid, String tenantId, List<MetaMenu> menus) {
        MetaUserinfo user = new MetaUserinfo();
        user.setId(uid);
        user.setAccount(eruptUpmsProp.getDefaultAccount());
        user.setUsername(eruptUpmsProp.getDefaultAccount());
        user.setSuperAdmin(true);
        user.setTenantId(tenantId);
        String token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, menus, token, 5);
        MetaContext.remove();
        MetaContext.register(new MetaUser(uid, user.getAccount(), user.getUsername(), tenantId));
        MetaContext.registerToken(token);
    }

}
