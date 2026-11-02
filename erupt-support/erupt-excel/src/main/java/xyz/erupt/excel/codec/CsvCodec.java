package xyz.erupt.excel.codec;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RFC 4180 CSV with a header row of titles. Written with a UTF-8 BOM so spreadsheet applications
 * open non-ASCII text correctly.
 */
@Component
public class CsvCodec implements TableCodec {

    private static final char BOM = '\uFEFF';

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public String name() {
        return "CSV";
    }

    @Override
    public String mediaType() {
        return "text/csv";
    }

    @Override
    public void write(TableSheet sheet, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(BOM);
        List<String> header = sheet.columns().stream().map(TableSheet.TableColumn::title).toList();
        this.line(writer, header);
        for (List<Object> row : sheet.rows()) {
            this.line(writer, row.stream().map(TextCodecSupport::text).toList());
        }
        writer.flush();
    }


    private void line(Writer writer, List<String> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) writer.write(',');
            writer.write(this.escape(cells.get(i)));
        }
        writer.write("\r\n");
    }

    private String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }


}
