package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockPart;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.R;
import xyz.erupt.excel.codec.TableCodecs;
import xyz.erupt.excel.codec.XlsxCodec;
import xyz.erupt.excel.service.EruptExcelService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeExcelService} against a node stubbed at the {@link CloudNodeRouter} seam: an export reads the node's
 * pages and shows choice and reference labels, an import maps labels back and adds row by row.
 *
 * @author YuePeng
 */
class NodeExcelServiceTest {

    private static final String BUILD = "{\"eruptModel\":{\"eruptName\":\"Article\",\"eruptJson\":{},\"eruptFieldModels\":["
            + "{\"fieldName\":\"title\",\"fieldReturnName\":\"String\",\"eruptFieldJson\":{\"views\":[{\"title\":\"Title\"}],"
            + "\"edit\":{\"title\":\"Title\",\"type\":\"INPUT\",\"notNull\":true}}},"
            + "{\"fieldName\":\"status\",\"fieldReturnName\":\"String\",\"componentValue\":[{\"value\":\"draft\",\"label\":\"Draft\"},"
            + "{\"value\":\"published\",\"label\":\"Published\"}],\"eruptFieldJson\":{\"views\":[{\"title\":\"Status\"}],"
            + "\"edit\":{\"title\":\"Status\",\"type\":\"CHOICE\"}}},"
            + "{\"fieldName\":\"category\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":{\"views\":[{\"title\":\"Category\",\"column\":\"name\"}],"
            + "\"edit\":{\"title\":\"Category\",\"type\":\"REFERENCE_TABLE\",\"referenceTableType\":{\"id\":\"id\",\"label\":\"name\"}}}}]}}";

    private final List<JsonObject> added = new ArrayList<>();

    private NodeExcelService service;

    private EruptBuildModel build;

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                assertTrue(enforced.contains(CloudSignature.Enforced.POWER));
                if (eruptName.equals("blog.Category")) {
                    return method == HttpMethod.GET ? "{\"id\":1,\"name\":\"News\"}" : "{\"list\":[{\"id\":1,\"name\":\"News\"}]}";
                }
                if (pathTemplate.contains("/table")) {
                    assertEquals(EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.QUERY), enforced);
                    return "{\"list\":[{\"title\":\"Hello\",\"status\":\"draft\",\"category\":1}]}";
                }
                assertEquals(EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD), enforced);
                added.add((JsonObject) body);
                return "{\"success\":true}";
            }
        };
        NodeReferenceService references = new NodeReferenceService();
        inject(references, "cloudNodeRouter", router);
        service = new NodeExcelService();
        inject(service, "cloudNodeRouter", router);
        inject(service, "nodeReferenceService", references);
        inject(service, "eruptExcelService", new EruptExcelService());
        inject(service, "tableCodecs", new TableCodecs(List.of(new XlsxCodec())));
        inject(service, "xlsxCodec", new XlsxCodec());
        build = GsonFactory.getGson().fromJson(BUILD, EruptBuildModel.class);
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static List<String> cells(Row row) {
        List<String> cells = new ArrayList<>();
        row.forEach(cell -> cells.add(cell.getStringCellValue()));
        return cells;
    }

    @Test
    void exportShowsLabels() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.export("blog.Article", build, "{}".getBytes(), new MockHttpServletRequest(), response);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(List.of("Title", "Status", "Category"), cells(sheet.getRow(0)));
            assertEquals(List.of("Hello", "Draft", "News"), cells(sheet.getRow(1)));
        }
    }

    @Test
    void importMapsLabelsBackAndAddsEachRow() throws Exception {
        MockHttpServletResponse template = new MockHttpServletResponse();
        service.template("blog.Article", build, new MockHttpServletRequest(), template);
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(template.getContentAsByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(List.of("Title", "Status", "Category"), cells(sheet.getRow(0)));
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("World");
            row.createCell(1).setCellValue("Published");
            row.createCell(2).setCellValue("News");
            wb.write(file);
        }
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        MockPart part = new MockPart("file", "articles.xlsx", file.toByteArray());
        request.addPart(part);
        R<Void> result = service.importData("blog.Article", build, request);
        assertTrue(result.isSuccess(), result.getMessage());
        assertEquals(1, added.size());
        JsonObject record = added.get(0);
        assertEquals("World", record.get("title").getAsString());
        assertEquals("published", record.get("status").getAsString());
        assertEquals("1", record.getAsJsonObject("category").get("id").getAsString());
    }

}
