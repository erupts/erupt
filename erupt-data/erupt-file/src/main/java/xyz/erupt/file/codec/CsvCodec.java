package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV whose first line is a header of field names. Values are read as strings and
 * coerced to field types downstream by Gson; empty cells become null. Suited to
 * flat models — nested fields belong in JSON or YAML. Parsing follows RFC 4180
 * quoting (doubled quotes, embedded commas / newlines).
 *
 * @author YuePeng
 */
public class CsvCodec implements FileCodec {

    @Override
    public FileType type() {
        return FileType.CSV;
    }

    @Override
    public boolean accept(String path) {
        return "csv".equals(FileCodec.extension(path));
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        List<List<String>> records = this.parse(content);
        List<JsonObject> list = new ArrayList<>();
        if (records.isEmpty()) return list;
        List<String> header = records.get(0);
        for (int i = 1; i < records.size(); i++) {
            JsonObject json = new JsonObject();
            List<String> record = records.get(i);
            for (int c = 0; c < header.size() && c < record.size(); c++) {
                // empty cells mean null so numeric fields do not choke on ""
                if (!record.get(c).isEmpty()) json.addProperty(header.get(c), record.get(c));
            }
            list.add(json);
        }
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        List<String> columns = new ArrayList<>();
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        if (!eruptModel.getEruptFieldMap().containsKey(primaryKey)) columns.add(primaryKey);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) columns.add(fieldModel.getFieldName());
        StringBuilder csv = new StringBuilder(String.join(",", columns)).append('\n');
        for (JsonObject json : records) {
            for (int c = 0; c < columns.size(); c++) {
                if (c > 0) csv.append(',');
                JsonElement value = json.get(columns.get(c));
                if (null != value && !value.isJsonNull()) csv.append(this.escape(FileCodec.asText(value)));
            }
            csv.append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private List<List<String>> parse(String content) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                record.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') i++;
                record.add(cell.toString());
                cell.setLength(0);
                records.add(record);
                record = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (cell.length() > 0 || !record.isEmpty()) {
            record.add(cell.toString());
            records.add(record);
        }
        records.removeIf(r -> r.size() == 1 && r.get(0).isEmpty());
        return records;
    }

}
