package xyz.erupt.generator.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.generator.model.input.DbImportModal;
import xyz.erupt.generator.service.DbIntrospectService;

import java.util.List;

/**
 * Suggests the columns of the tables picked in the form, read when the list is
 * opened so that changing the selection changes the suggestions.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class DbColumnTags implements TagsFetchHandler<DbImportModal> {

    @Resource
    private DbIntrospectService dbIntrospectService;

    @Override
    public List<String> fetchTags(DbImportModal modal, String[] params) {
        return dbIntrospectService.columns(modal);
    }

}
