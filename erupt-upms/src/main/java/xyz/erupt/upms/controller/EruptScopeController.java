package xyz.erupt.upms.controller;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.scope.EruptScopeService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.upms.service.EruptUserService;

import java.util.*;

/**
 * Scopes for the page header. A scope is offered when at least one model that declares it is on
 * the user's menus; that model is the parent its form loads under.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/scope")
public class EruptScopeController {

    @Resource
    private EruptScopeService eruptScopeService;

    @Resource
    private EruptUserService eruptUserService;

    @Getter
    public static class ScopeView {

        /** Simple class name of the scope form */
        private final String name;

        /** Header label: the scope form's @Erupt name */
        private final String title;

        /** A model on the user's menus that declares the scope: send it as eruptParent to load the form */
        private final String parent;

        /** Models on the user's menus that declare the scope: the header shows it only on their pages */
        private final List<String> models;

        /** The scope form's condition fields, in form order */
        private final List<String> fields;

        /** The defaults, signed: what the header holds until the user picks */
        private final EruptScopeService.Accepted defaults;

        ScopeView(String name, String title, List<String> models, List<String> fields, EruptScopeService.Accepted defaults) {
            this.name = name;
            this.title = title;
            this.parent = models.get(0);
            this.models = models;
            this.fields = fields;
            this.defaults = defaults;
        }
    }

    /**
     * Scopes visible to the current user, each with its signed defaults.
     */
    @GetMapping
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public List<ScopeView> scopes() {
        List<ScopeView> views = new ArrayList<>();
        this.visible().forEach((scope, models) -> {
            EruptModel model = EruptScopeService.scopeModel(scope);
            EruptScopeService.Accepted defaults;
            try {
                defaults = eruptScopeService.acceptDefaults(scope);
            } catch (EruptWebApiRuntimeException e) {
                // No valid default (a required choice with no options): the page asks the user instead
                defaults = null;
            }
            views.add(new ScopeView(scope.getSimpleName(), I18nTranslate.$translate(model.getErupt().name()), models,
                    EruptScopeService.scopeFields(model).stream().map(it -> it.getFieldName()).toList(), defaults));
        });
        return views;
    }

    /**
     * Checks a submitted scope form and signs it; send the token back in the erupt-scope header.
     */
    @PostMapping("/{scope}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptScopeService.Accepted pick(@PathVariable("scope") String scopeName, @RequestBody JsonObject form) {
        Class<?> scope = this.visible().keySet().stream().filter(it -> it.getSimpleName().equals(scopeName)).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.scope.not_found")));
        return eruptScopeService.accept(scope, form);
    }

    // Scope class -> the models on the user's menus that declare it
    private Map<Class<?>, List<String>> visible() {
        Map<Class<?>, List<String>> scopes = new LinkedHashMap<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            EruptScope scope = EruptScopeService.scopeOf(model);
            if (null == scope) continue;
            if (!model.getErupt().authVerify() || null != eruptUserService.getEruptMenuByValue(model.getEruptName())) {
                scopes.computeIfAbsent(scope.value(), k -> new ArrayList<>()).add(model.getEruptName());
            }
        }
        return scopes;
    }

}
