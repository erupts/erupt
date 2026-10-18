package xyz.erupt.filecenter.web;

import org.springframework.stereotype.Component;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.filecenter.prop.EruptFileCenterProperties;
import xyz.erupt.filecenter.store.FileStores;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The page. Its menu value is the template name; the write button is a menu of its own.
 *
 * @author YuePeng
 */
@EruptTpl
@Component
public class FileCenterTpl {

    public static final String PAGE = "file-center.ftl";

    public static final String WRITE = "file-center@write";

    private static final List<String> LABELS = List.of("file.upload", "file.new_folder", "file.rename", "file.delete", "file.download",
            "file.copy_link", "file.search", "file.empty", "file.size", "file.modified", "file.type", "file.confirm_delete",
            "file.name", "file.link_copied", "file.items", "file.read_only", "file.too_large", "file.open");

    private final FileStores stores;

    private final EruptFileCenterProperties properties;

    private final EruptContextService contextService;

    private final EruptUserService userService;

    public FileCenterTpl(FileStores stores, EruptFileCenterProperties properties, EruptContextService contextService, EruptUserService userService) {
        this.stores = stores;
        this.properties = properties;
        this.contextService = contextService;
        this.userService = userService;
    }

    @TplAction(PAGE)
    public Map<String, Object> page() {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("token", contextService.getCurrentToken());
        model.put("writable", null != userService.getEruptMenuByValue(WRITE));
        model.put("stores", stores.names());
        model.put("textPreviewBytes", properties.getTextPreviewBytes());
        Map<String, String> labels = new LinkedHashMap<>();
        for (String key : LABELS) labels.put(key.substring("file.".length()), I18nTranslate.$translate(key));
        model.put("labels", labels);
        return model;
    }

}
