package xyz.erupt.excel.codec;


import java.io.IOException;
import java.io.OutputStream;

/**
 * One export file format. A codec only deals with the encoding surface: it receives a
 * {@link TableSheet} whose cells are already display values (choice labels, boolean wording,
 * reference labels) and writes them out. Import is Excel only and lives in {@link XlsxCodec}.
 * Register a Spring bean implementing this to add a format.
 *
 * @author YuePeng
 */
public interface TableCodec {

    // lower-case file extension; doubles as the format key the client sends
    String format();

    // shown to users next to the export button
    String name();

    String mediaType();

    /**
     * Write the sheet. A {@link TableSheet#template() template} has no rows and edit-based
     * columns; a codec may decorate it (validation, hints) or just emit the header.
     */
    void write(TableSheet sheet, OutputStream out) throws IOException;


}
