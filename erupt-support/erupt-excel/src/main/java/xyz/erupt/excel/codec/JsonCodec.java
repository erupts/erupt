package xyz.erupt.excel.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A JSON array of objects keyed by column title, holding the same display values the table shows.
 */
@Component
public class JsonCodec implements TableCodec {

    @Override
    public String format() {
        return "json";
    }

    @Override
    public String name() {
        return "JSON";
    }

    @Override
    public String mediaType() {
        return "application/json";
    }

    @Override
    public void write(TableSheet sheet, OutputStream out) throws IOException {
        JsonArray array = new JsonArray();
        for (List<Object> row : sheet.rows()) array.add(object(sheet, row));
        out.write(GsonFactory.getGson().toJson(array).getBytes(StandardCharsets.UTF_8));
    }


    // One row as an object keyed by column title: numbers and booleans as such, the rest as text
    static JsonObject object(TableSheet sheet, List<Object> row) {
        JsonObject json = new JsonObject();
        for (int c = 0; c < sheet.columns().size(); c++) {
            Object value = row.get(c);
            String title = sheet.columns().get(c).title();
            if (value instanceof Number n) {
                json.addProperty(title, n);
            } else if (value instanceof Boolean b) {
                json.addProperty(title, b);
            } else {
                json.addProperty(title, null == value ? null : TextCodecSupport.text(value));
            }
        }
        return json;
    }


}
