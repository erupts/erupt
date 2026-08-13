package xyz.erupt.file;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;
import xyz.erupt.file.codec.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Codec-level round trips: decode is model-free, and JSON / YAML encode ignore the
 * model, so these run without booting the erupt runtime.
 *
 * @author YuePeng
 */
public class FileCodecTest {

    @EruptFile("x.json")
    static class ListForm {
    }

    @EruptFile(value = "x.json", single = true)
    static class SingleForm {
    }

    private final EruptFile list = ListForm.class.getAnnotation(EruptFile.class);

    private final EruptFile single = SingleForm.class.getAnnotation(EruptFile.class);

    @Test
    void codecsReportTheirType() {
        assertEquals(FileType.CSV, new CsvCodec().type());
        assertEquals(FileType.JSON, new JsonCodec().type());
        assertEquals(FileType.YAML, new YamlCodec().type());
        assertEquals(FileType.PROPERTIES, new PropertiesCodec().type());
        assertEquals(FileType.MARKDOWN, new MarkdownCodec().type());
    }

    @Test
    void csvHandlesQuotedCells() {
        List<JsonObject> rows = new CsvCodec().decode("name,note\nfoo,\"a,b\"\nbar,\"line1\nline2\"");
        assertEquals(2, rows.size());
        assertEquals("a,b", rows.get(0).get("note").getAsString());
        assertEquals("line1\nline2", rows.get(1).get("note").getAsString());
        // empty cell stays absent so numeric fields do not see ""
        assertFalse(new CsvCodec().decode("name,age\nfoo,").get(0).has("age"));
    }

    @Test
    void jsonReadsBothShapesAndWritesByFlag() {
        JsonCodec codec = new JsonCodec();
        assertEquals(1, codec.decode("[{\"a\":1}]").size());
        assertEquals(1, codec.decode("{\"a\":1}").size());
        List<JsonObject> records = codec.decode("{\"a\":1}");
        assertTrue(codec.encode(null, single, records).startsWith("{"));
        assertTrue(codec.encode(null, list, records).startsWith("["));
    }

    @Test
    void yamlReadsSequenceAndMapping() {
        YamlCodec codec = new YamlCodec();
        assertEquals(2, codec.decode("- id: 1\n  name: a\n- id: 2\n  name: b").size());
        List<JsonObject> record = codec.decode("id: 1\nname: a");
        assertEquals(1, record.size());
        assertEquals("a", record.get(0).get("name").getAsString());
        // integral numbers stay clean on the way out (id: 1, not id: 1.0)
        assertTrue(codec.encode(null, single, record).contains("id: 1\n"));
    }

    @Test
    void propertiesMapToSingleRecord() {
        PropertiesCodec codec = new PropertiesCodec();
        assertTrue(codec.singleton(single));
        List<JsonObject> rows = codec.decode("host=localhost\nport=8080");
        assertEquals(1, rows.size());
        assertEquals("localhost", rows.get(0).get("host").getAsString());
        assertEquals("8080", rows.get(0).get("port").getAsString());
    }

    @Test
    void markdownSplitsFrontMatterAndBody() {
        MarkdownCodec codec = new MarkdownCodec();
        assertTrue(codec.singleton(single));
        JsonObject row = codec.decode("---\ntitle: Hello\npublished: true\n---\nbody line one\nbody line two").get(0);
        assertEquals("Hello", row.get("title").getAsString());
        assertEquals("true", row.get("published").getAsString());
        assertEquals("body line one\nbody line two", row.get(MarkdownCodec.BODY_FIELD).getAsString());
        // no front matter → whole file is the body
        assertEquals("just text", codec.decode("just text").get(0).get(MarkdownCodec.BODY_FIELD).getAsString());
    }

}
