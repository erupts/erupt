package xyz.erupt.test.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.core.AiToolboxManager;
import xyz.erupt.ai_claw.tool.EruptModelTools;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.atlas.constant.AtlasConstant;
import xyz.erupt.atlas.tool.AtlasTools;
import xyz.erupt.comment.model.EruptRecordComment;
import xyz.erupt.comment.tool.CommentTools;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.notice.model.NoticeLog;
import xyz.erupt.notice.model.NoticeLogDetail;
import xyz.erupt.notice.model.NoticeScene;
import xyz.erupt.notice.tool.NoticeTools;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;
import xyz.erupt.revision.tool.RevisionTools;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.test.model.erupt.RowOperationModel;
import xyz.erupt.test.model.erupt.TestOperationHandler;
import xyz.erupt.tpl.service.EruptTplService;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The AI tools the plugin modules bring along: each is reached the way an agent reaches it, by
 * name with json arguments through {@link AiToolboxManager}, in the session of the user it acts
 * for — and each asks for the same right its page does.
 */
class ModuleToolsTest extends EruptApplicationTests {

    private static final String SCENE = "ut-tool-scene";

    private static final String ORDERS = OrderByModel.class.getSimpleName();

    private static final String NOTICE_LOG = NoticeLog.class.getSimpleName();

    private static final String RECORD = "ut-tool-record";

    private static final String OPERATIONS = RowOperationModel.class.getSimpleName();

    private static final String APPROVE = "approve";

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private NoticeTools noticeTools;

    @Resource
    private AtlasTools atlasTools;

    @Resource
    private CommentTools commentTools;

    @Resource
    private EruptModelTools modelTools;

    @Resource
    private RevisionTools revisionTools;

    @Resource
    private EruptModifyService modifyService;

    private Long uid;

    @BeforeEach
    void setUp() {
        uid = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one().getId();
        this.cleanUp();
        transactionTemplate.executeWithoutResult(status -> {
            NoticeScene scene = new NoticeScene();
            scene.setCode(SCENE);
            scene.setName(SCENE);
            eruptDao.persist(scene);
        });
    }

    @AfterEach
    void cleanUp() {
        MetaContext.remove();
        transactionTemplate.executeWithoutResult(status -> {
            NoticeScene scene = eruptDao.lambdaQuery(NoticeScene.class).eq(NoticeScene::getCode, SCENE).one();
            if (null != scene) {
                for (NoticeLog log : eruptDao.lambdaQuery(NoticeLog.class).eq(NoticeLog::getNoticeScene, scene).list()) {
                    eruptDao.lambdaQuery(NoticeLogDetail.class).eq(NoticeLogDetail::getNoticeLog, log).delete();
                    eruptDao.delete(eruptDao.find(NoticeLog.class, log.getId()));
                }
                eruptDao.delete(eruptDao.find(NoticeScene.class, scene.getId()));
            }
            eruptDao.lambdaQuery(EruptRecordComment.class).eq(EruptRecordComment::getRecordId, RECORD).delete();
            eruptDao.lambdaQuery(RowOperationModel.class).eq(RowOperationModel::getTitle, RECORD).delete();
            eruptDao.lambdaQuery(OrderByModel.class).eq(OrderByModel::getName, RECORD).delete();
            eruptDao.lambdaQuery(EruptRecordRevision.class).eq(EruptRecordRevision::getErupt, ORDERS).delete();
        });
    }

    @Test
    void theModuleToolsAreOnTheToolbox() {
        assertTrue(AiToolboxManager.getAiMethodMap().keySet().containsAll(List.of("listNoticeOptions", "sendNotice",
                "atlasGraph", "atlasModelDetail", "atlasPowerAudit", "listRecordComments", "addRecordComment", "findUsers",
                "eruptOperations", "invokeEruptOperation", "listRecordRevisions", "rollbackRecordRevision", "restoreDeletedRecord")));
    }

    @Test
    void aNoticeIsSentToTheUsersNamedAndAnUnknownOneIsRefused() {
        this.login(true);
        JsonObject options = this.call("listNoticeOptions", new JsonObject()).getAsJsonObject();
        assertTrue(options.getAsJsonArray("scenes").toString().contains(SCENE));
        assertFalse(options.getAsJsonArray("channels").isEmpty(), "the in-app channel is always there");

        JsonObject args = new JsonObject();
        args.addProperty("scene", SCENE);
        args.add("userIds", ids(uid));
        args.addProperty("title", "Done");
        args.addProperty("content", "The import finished");
        JsonObject sent = this.call("sendNotice", args).getAsJsonObject();
        assertEquals(1, sent.get("deliveries").getAsInt());
        assertEquals(0, sent.get("failed").getAsInt());
        NoticeLog log = eruptDao.find(NoticeLog.class, sent.get("noticeLogId").getAsLong());
        assertEquals("Done", log.getTitle());
        assertEquals(1, eruptDao.lambdaQuery(NoticeLogDetail.class).eq(NoticeLogDetail::getNoticeLog, log).count());

        assertThrows(EruptWebApiRuntimeException.class,
                () -> noticeTools.sendNotice(SCENE, List.of(uid, -404L), "Done", "x", null, null));
        assertThrows(EruptWebApiRuntimeException.class,
                () -> noticeTools.sendNotice(SCENE, List.of(uid), "Done", "x", null, List.of("NoSuchChannel")));
        assertEquals(1, eruptDao.lambdaQuery(NoticeLog.class).eq(NoticeLog::getNoticeScene, log.getNoticeScene()).count(),
                "a refused notice writes no log");
    }

    @Test
    void sendingNeedsTheAddPowerOfTheNoticeLog() {
        this.login(false);
        assertThrows(EruptWebApiRuntimeException.class, () -> noticeTools.sendNotice(SCENE, List.of(uid), "Done", "x", null, null));
        assertThrows(EruptWebApiRuntimeException.class, () -> noticeTools.listNoticeOptions());
    }

    @Test
    void theAtlasIsReadAsAGraphAndAModelAtATime() {
        this.login(true);
        JsonObject graph = this.call("atlasGraph", new JsonObject()).getAsJsonObject();
        assertTrue(graph.getAsJsonArray("nodes").toString().contains("\"" + NOTICE_LOG + "\""));
        assertTrue(graph.has("edges") && graph.has("audit"));
        assertTrue(graph.get("text").isJsonNull(), "the page's translations are not the agent's business");

        JsonObject args = new JsonObject();
        args.addProperty("model", NOTICE_LOG);
        JsonObject detail = this.call("atlasModelDetail", args).getAsJsonObject();
        assertEquals(NOTICE_LOG, detail.get("name").getAsString());
        assertFalse(detail.getAsJsonArray("fields").isEmpty());
        assertTrue(this.call("atlasPowerAudit", new JsonObject()).isJsonArray());
        assertThrows(EruptWebApiRuntimeException.class, () -> atlasTools.atlasModelDetail("NoSuchModel"));
    }

    @Test
    void aCommentIsWrittenAndReadBackOnARecord() {
        this.login(true);
        JsonObject args = new JsonObject();
        args.addProperty("eruptName", ORDERS);
        args.addProperty("recordId", RECORD);
        args.addProperty("content", "Checked by the agent");
        args.add("mentions", ids(uid));
        JsonObject head = this.call("addRecordComment", args).getAsJsonObject();
        assertEquals(uid, head.get("userId").getAsLong());
        assertEquals(1, head.getAsJsonArray("mentions").size());

        args.remove("mentions");
        args.addProperty("content", "And fixed");
        args.addProperty("parentId", head.get("id").getAsLong());
        this.call("addRecordComment", args);

        JsonObject read = new JsonObject();
        read.addProperty("eruptName", ORDERS);
        read.addProperty("recordId", RECORD);
        JsonArray comments = this.call("listRecordComments", read).getAsJsonArray();
        assertEquals(2, comments.size());
        assertEquals(head.get("id").getAsLong(), comments.get(1).getAsJsonObject().get("parentId").getAsLong());
    }

    @Test
    void aRowOperationIsListedAndRunOnTheRowsItsModeAsksFor() {
        this.login(true);
        Long first = this.operationRow();
        Long second = this.operationRow();
        JsonObject args = new JsonObject();
        args.addProperty("eruptName", OPERATIONS);
        JsonArray operations = this.call("eruptOperations", args).getAsJsonArray();
        assertEquals(4, operations.size());
        JsonObject approve = operations.asList().stream().map(JsonElement::getAsJsonObject)
                .filter(it -> APPROVE.equals(it.get("code").getAsString())).findFirst().orElseThrow();
        assertEquals(RowOperation.Mode.SINGLE.name(), approve.get("mode").getAsString());
        assertTrue(approve.get("ifExpr").getAsString().contains("PENDING"), "the row condition is handed to the agent");
        assertFalse(approve.get("confirm").isJsonNull(), "and so is the prompt a user would have to confirm");

        TestOperationHandler.lastRows = null;
        args.addProperty("code", APPROVE);
        args.add("ids", ids(first));
        AiToolboxManager.invoke(ToolExecutionRequest.builder().name("invokeEruptOperation").arguments(args.toString()).build());
        assertEquals(List.of(first), TestOperationHandler.lastRows.stream().map(RowOperationModel::getId).toList());

        TestOperationHandler.lastRows = null;
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.invokeEruptOperation(OPERATIONS, APPROVE,
                List.of(String.valueOf(first), String.valueOf(second)), null), "a single-row operation takes one row");
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.invokeEruptOperation(OPERATIONS, "batchDelete", null, null),
                "a row operation without a row would be answered as done without running");
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.invokeEruptOperation(OPERATIONS, "no-such", null, null));
        assertNull(TestOperationHandler.lastRows, "none of the refused calls reached the handler");

        modelTools.invokeEruptOperation(OPERATIONS, "batchDelete", List.of(String.valueOf(first), String.valueOf(second)), null);
        assertEquals(2, TestOperationHandler.lastRows.size());
        // a BUTTON operation depends on no row, and the handler is handed none
        modelTools.invokeEruptOperation(OPERATIONS, "import", null, null);
        assertNull(TestOperationHandler.lastRows);
    }

    @Test
    void aChangeIsReadFromTheHistoryUndoneAndADeletedRecordBroughtBack() {
        this.login(true);
        EruptModel orders = EruptCoreService.getErupt(ORDERS);
        MetaContext.register(new MetaErupt(ORDERS, ORDERS));
        JsonObject data = new JsonObject();
        data.addProperty("name", RECORD);
        data.addProperty("remark", "v1");
        Long id = (Long) modifyService.insertEruptData(orders, data);
        data.addProperty("id", id);
        data.addProperty("remark", "v2");
        modifyService.updateEruptData(orders, data);

        JsonObject args = new JsonObject();
        args.addProperty("eruptName", ORDERS);
        args.addProperty("recordId", String.valueOf(id));
        JsonArray revisions = this.call("listRecordRevisions", args).getAsJsonArray();
        assertEquals(2, revisions.size());
        JsonObject update = revisions.get(0).getAsJsonObject();
        assertEquals(RevisionOperation.UPDATE.name(), update.get("operation").getAsString());

        args.addProperty("revisionId", update.get("id").getAsLong());
        JsonObject undone = this.call("rollbackRecordRevision", args).getAsJsonObject();
        assertEquals(3, undone.get("version").getAsInt(), "the rollback is a revision of its own");
        assertEquals("v1", eruptDao.find(OrderByModel.class, id).getRemark());

        MetaContext.register(new MetaErupt(ORDERS, ORDERS));
        modifyService.deleteEruptData(orders, List.of(id), false);
        Long restored = Long.valueOf(revisionTools.restoreDeletedRecord(ORDERS, String.valueOf(id)));
        assertEquals(RECORD, eruptDao.find(OrderByModel.class, restored).getName());
    }

    @Test
    void aModelOrAPageTheUserCannotOpenIsClosedToTheToolsToo() {
        this.login(false);
        assertThrows(EruptWebApiRuntimeException.class, () -> commentTools.listRecordComments(ORDERS, RECORD));
        assertThrows(EruptWebApiRuntimeException.class, () -> commentTools.addRecordComment(ORDERS, RECORD, "x", null, null));
        assertThrows(EruptWebApiRuntimeException.class, () -> atlasTools.atlasGraph());
        assertThrows(EruptWebApiRuntimeException.class, () -> atlasTools.atlasPowerAudit());
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.eruptOperations(OPERATIONS));
        assertThrows(EruptWebApiRuntimeException.class, () -> modelTools.invokeEruptOperation(OPERATIONS, "import", null, null));
        assertThrows(EruptWebApiRuntimeException.class, () -> revisionTools.listRecordRevisions(ORDERS, RECORD));
        assertThrows(EruptWebApiRuntimeException.class, () -> revisionTools.restoreDeletedRecord(ORDERS, RECORD));
    }

    // A session of the default user; granted, it holds the notice log with its add button, the atlas page and one model
    private void login(boolean granted) {
        MetaUserinfo user = new MetaUserinfo();
        user.setId(uid);
        user.setAccount(eruptUpmsProp.getDefaultAccount());
        user.setUsername(eruptUpmsProp.getDefaultAccount());
        List<MetaMenu> menus = new ArrayList<>();
        if (granted) {
            MetaMenu noticeLog = MetaMenu.createEruptClassMenu(NoticeLog.class, null, 1);
            menus.add(noticeLog);
            menus.add(MetaMenu.createSimpleMenu("ut-notice-add", "Add", NOTICE_LOG + "@ADD", noticeLog, 1, "button"));
            menus.add(MetaMenu.createSimpleMenu("ut-atlas", "Model Graph", AtlasConstant.MENU_ATLAS, null, 2, EruptTplService.TPL));
            MetaMenu orders = MetaMenu.createEruptClassMenu(OrderByModel.class, null, 3);
            menus.add(orders);
            for (String power : List.of("ADD", "EDIT", "DELETE")) {
                menus.add(MetaMenu.createSimpleMenu("ut-orders-" + power, power, ORDERS + "@" + power, orders, 1, "button"));
            }
            menus.add(MetaMenu.createEruptClassMenu(RowOperationModel.class, null, 4));
        }
        String token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, menus, token, 5);
        MetaContext.register(new MetaUser(uid, user.getAccount(), user.getUsername()));
        MetaContext.registerToken(token);
    }

    private Long operationRow() {
        return transactionTemplate.execute(status -> {
            RowOperationModel row = new RowOperationModel();
            row.setTitle(RECORD);
            eruptDao.persist(row);
            return row.getId();
        });
    }

    private JsonElement call(String tool, JsonObject args) {
        return JsonParser.parseString(String.valueOf(AiToolboxManager.invoke(
                ToolExecutionRequest.builder().name(tool).arguments(args.toString()).build())));
    }

    private static JsonArray ids(Long... ids) {
        JsonArray array = new JsonArray();
        for (Long id : ids) array.add(id);
        return array;
    }

}
