package xyz.erupt.generator.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.generator.model.input.DbImportModal;
import xyz.erupt.generator.service.DbIntrospectService;

import java.util.List;

/**
 * Feeds the three linked selects of the import form, the option set each one asks for
 * is named by its fetchHandlerParams.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class DbChoiceFetch implements ChoiceFetchHandler<DbImportModal> {

    public static final String DATA_SOURCE = "dataSource";

    public static final String NAMESPACE = "namespace";

    public static final String TABLE = "table";

    @Resource
    private DbIntrospectService dbIntrospectService;

    @Override
    public List<VLModel> fetch(String[] params) {
        return this.fetch(new DbImportModal(), params);
    }

    @Override
    public List<VLModel> fetchFilter(DbImportModal modal, String[] params) {
        return this.fetch(modal, params);
    }

    private List<VLModel> fetch(DbImportModal modal, String[] params) {
        String scope = params.length > 0 ? params[0] : TABLE;
        return switch (scope) {
            case DATA_SOURCE -> dbIntrospectService.dataSources();
            case NAMESPACE -> dbIntrospectService.namespaces(modal.getDataSource());
            default -> dbIntrospectService.tables(modal.getDataSource(), modal.getNamespace());
        };
    }

}
