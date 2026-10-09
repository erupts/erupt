package xyz.erupt.excel.codec;

import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JSON Lines: one object per line, the same objects {@link JsonCodec} puts in its array. Streams
 * row by row in both directions, so a large export never holds the whole document in memory and
 * the file feeds straight into log pipelines and LLM tooling.
 */
@Component
public class JsonlCodec implements TableCodec {

    @Override
    public String format() {
        return "jsonl";
    }

    @Override
    public String name() {
        return "JSON Lines";
    }

    @Override
    public String mediaType() {
        return "application/jsonl";
    }

    @Override
    public void write(TableSheet sheet, OutputStream out) throws IOException {
        for (List<Object> row : sheet.rows()) {
            out.write(GsonFactory.getGson().toJson(JsonCodec.object(sheet, row)).getBytes(StandardCharsets.UTF_8));
            out.write('\n');
        }
    }


}
