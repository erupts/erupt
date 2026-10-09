package xyz.erupt.excel.codec;

import org.springframework.stereotype.Component;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The codecs on the classpath, keyed by format. Excel stays the default so existing clients that
 * send no format keep getting what they got.
 */
@Component
public class TableCodecs {

    public static final String DEFAULT = XlsxCodec.FORMAT;

    private final Map<String, TableCodec> codecs;

    public TableCodecs(List<TableCodec> codecs) {
        this.codecs = codecs.stream()
                .sorted(Comparator.comparing(TableCodec::format))
                .collect(Collectors.toMap(TableCodec::format, Function.identity(), (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public List<TableCodec> list() {
        return List.copyOf(codecs.values());
    }

    public TableCodec get(String format) {
        TableCodec codec = codecs.get(null == format || format.isBlank() ? DEFAULT : format.toLowerCase());
        if (null == codec) throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.unsupported_format"), format));
        return codec;
    }


}
