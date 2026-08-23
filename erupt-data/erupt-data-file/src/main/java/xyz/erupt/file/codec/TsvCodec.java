package xyz.erupt.file.codec;

import xyz.erupt.file.annotation.FileType;

/**
 * Tab-separated values — a {@link CsvCodec} whose delimiter is a tab. The first
 * line is a header of field names and the same RFC 4180 quoting applies (values
 * containing a tab, quote or newline are wrapped in double quotes). Suits flat
 * models and pastes cleanly to and from spreadsheets.
 *
 * @author YuePeng
 */
public class TsvCodec extends CsvCodec {

    @Override
    public FileType type() {
        return FileType.TSV;
    }

    @Override
    public boolean accept(String path) {
        return "tsv".equals(FileCodec.extension(path));
    }

    @Override
    protected char delimiter() {
        return '\t';
    }

}
