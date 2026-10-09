package xyz.erupt.test.excel;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.excel.codec.TableCodec;
import xyz.erupt.excel.codec.TableCodecs;
import xyz.erupt.excel.codec.XlsxCodec;
import xyz.erupt.excel.codec.TableSheet;
import xyz.erupt.excel.service.EruptExcelService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.TypeInferenceModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Export is one sheet in several encodings, every codec writing the same display values; import is
 * Excel only and maps an export back to identical records.
 */
public class TableCodecTest extends EruptApplicationTests {

    @Resource
    private EruptExcelService excelService;

    @Resource
    private TableCodecs codecs;

    @Resource
    private XlsxCodec xlsxCodec;

    private EruptModel model() {
        return EruptCoreService.getErupt(TypeInferenceModel.class.getSimpleName());
    }

    private TableSheet sheet() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1L);
        row.put("created", LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        row.put("day", LocalDate.of(2026, 1, 2));
        row.put("attrs", "{\"a\":\"1,2\"}");
        row.put("score", 7);
        Page page = new Page();
        page.setList(List.of(row));
        return excelService.sheet(this.model(), page, null);
    }

    private byte[] write(String format, TableSheet sheet) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codecs.get(format).write(sheet, out);
        return out.toByteArray();
    }

    @Test
    public void formatsTest() {
        List<String> formats = codecs.list().stream().map(TableCodec::format).toList();
        assertTrue(formats.containsAll(List.of("xlsx", "csv", "json", "jsonl", "md")));
        assertEquals("xlsx", codecs.get(null).format());
        assertTrue(xlsxCodec.accept("data.XLS"));
        assertFalse(xlsxCodec.accept("data.csv"));
        assertThrows(Exception.class, () -> codecs.get("pdf"));
    }

    @Test
    public void textFormatsRenderDisplayValuesTest() throws Exception {
        TableSheet sheet = this.sheet();
        String csv = new String(this.write("csv", sheet), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("﻿Created,Day,"), csv);
        assertTrue(csv.contains("2026-01-02 03:04:05,2026-01-02,"), csv);
        assertTrue(csv.contains("\"{\"\"a\"\":\"\"1,2\"\"}\""), csv);
        String json = new String(this.write("json", sheet), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"Created\":\"2026-01-02 03:04:05\""), json);
        String md = new String(this.write("md", sheet), StandardCharsets.UTF_8);
        assertTrue(md.startsWith("| Created | Day |"), md);
        assertTrue(md.contains("| --- |"), md);
    }

    // an Excel export imports back as the same records; its view-only columns (Score) are skipped
    @Test
    public void excelRoundTripTest() throws Exception {
        TableSheet sheet = this.sheet();
        assertTrue(sheet.columns().stream().anyMatch(c -> c.title().equals("Score")));
        byte[] bytes = this.write("xlsx", sheet);
        List<Map<String, String>> rows = xlsxCodec.read(this.model(), new ByteArrayInputStream(bytes));
        assertEquals(1, rows.size());
        assertTrue(rows.get(0).containsKey("Score"));
        List<JsonObject> records = excelService.records(this.model(), rows);
        assertEquals(1, records.size());
        JsonObject record = records.get(0);
        assertEquals("2026-01-02T03:04:05.000", record.get("created").getAsString());
        assertEquals("2026-01-02T00:00:00.000", record.get("day").getAsString());
        assertEquals("{\"a\":\"1,2\"}", record.get("attrs").getAsString());
        assertFalse(record.has("score"));
    }

    @Test
    public void templateHasFormColumnsOnlyTest() throws Exception {
        TableSheet template = excelService.template(this.model());
        assertTrue(template.template());
        List<String> titles = template.columns().stream().map(TableSheet.TableColumn::title).toList();
        assertTrue(titles.contains("Created"));
        assertFalse(titles.contains("Items"), "a TAB_TABLE_ADD column cannot be filled from a sheet");
        String csv = new String(this.write("csv", template), StandardCharsets.UTF_8);
        assertEquals(1, csv.strip().lines().count());
        assertTrue(this.write("xlsx", template).length > 0);
    }


    @Test
    public void unknownColumnIsReportedTest() {
        Map<String, String> row = Map.of("Nope", "x");
        Exception e = assertThrows(Exception.class, () -> excelService.records(this.model(), List.of(row)));
        assertTrue(e.getMessage().contains("Nope"));
    }

}
