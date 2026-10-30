package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.service.EruptShareService;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Collection;
import java.util.Map;


/**
 * Issues the code and freezes the issuer when a link is created, checks the issuer may share
 * the target, and turns the password typed into the form into its digest.
 *
 * @author YuePeng
 */
@Component
public class ShareLinkProxy implements DataProxy<EruptShareLink> {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptShareService eruptShareService;

    @Override
    public void beforeAdd(EruptShareLink link) {
        eruptShareService.verifyTarget(link);
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        link.setCode(Erupts.generateCode(EruptShareLink.CODE_LENGTH));
        link.setTenantId(UpmsTenant.normalize(user.getTenantId()));
        link.setIssuerId(user.getId());
        link.setIssuerAccount(user.getAccount());
        link.setIssuerName(user.getUsername());
        link.setVisitCount(0);
        link.setLastVisitTime(null);
        // the form may leave the switch out; a new link is live
        if (null == link.getEnabled()) link.setEnabled(true);
        link.setPasswordHash(EruptShareService.digest(link.getCode(), link.getPassword()));
        link.setPassword(null);
    }

    // The update pipeline copies only editable fields onto the stored row, so the code, the
    // counters and the issuer are what they were; the password alone needs turning into its digest
    @Override
    public void beforeUpdate(EruptShareLink link) {
        eruptShareService.verifyTarget(link);
        if (StringUtils.isNotBlank(link.getPassword())) {
            link.setPasswordHash(EruptShareService.digest(link.getCode(), link.getPassword()));
        }
        link.setPassword(null);
        if (null == link.getEnabled()) link.setEnabled(true);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String type = LambdaSee.field(EruptShareLink::getType);
        String erupt = LambdaSee.field(EruptShareLink::getErupt);
        String path = LambdaSee.field(EruptShareLink::getPath);
        String target = LambdaSee.field(EruptShareLink::getTarget);
        for (Map<String, Object> row : list) {
            if (EruptShareLink.TYPE_FORM.equals(row.get(type))) {
                Object name = row.get(erupt);
                EruptModel model = null == name ? null : EruptCoreService.getErupt(name.toString());
                // a model that has since gone still names itself
                row.put(target, null == model ? name : I18nTranslate.$translate(model.getErupt().name()));
            } else {
                row.put(target, row.get(path));
            }
        }
    }

    @Override
    public void beforeDelete(EruptShareLink link) {
        // sessions already opened from this link end with it
        eruptShareService.revokeSessions(link.getCode());
    }

    @Override
    public void afterUpdate(EruptShareLink link) {
        if (!Boolean.TRUE.equals(link.getEnabled())) eruptShareService.revokeSessions(link.getCode());
    }
}
