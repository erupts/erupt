package xyz.erupt.excel.codec;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A GitHub-flavoured Markdown table, for pasting into documents and chats. Export only.
 */
@Component
public class MarkdownCodec implements TableCodec {

    @Override
    public String format() {
        return "md";
    }

    @Override
    public String name() {
        return "Markdown";
    }

    @Override
    public String mediaType() {
        return "text/markdown";
    }


    @Override
    public void write(TableSheet sheet, OutputStream out) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("| ");
        for (TableSheet.TableColumn column : sheet.columns()) md.append(this.cell(column.title())).append(" | ");
        md.append('\n').append("| ");
        for (int c = 0; c < sheet.columns().size(); c++) md.append("--- | ");
        md.append('\n');
        for (List<Object> row : sheet.rows()) {
            md.append("| ");
            for (Object value : row) md.append(this.cell(TextCodecSupport.text(value))).append(" | ");
            md.append('\n');
        }
        out.write(md.toString().getBytes(StandardCharsets.UTF_8));
    }


    private String cell(String text) {
        return text.replace("|", "\\|").replace("\r", "").replace("\n", "<br>");
    }

}
