package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.service.EruptShareService;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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

    @Resource
    private EruptDao eruptDao;

    // A week is the expiry the add form opens with; "now" in the picker would make a link that is dead on arrival
    private static final long DEFAULT_DAYS = 7;

    @Override
    public void addBehavior(EruptShareLink link) {
        link.setExpireTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DEFAULT_DAYS)));
    }

    @Override
    public void beforeAdd(EruptShareLink link) {
        eruptShareService.verifyTarget(link);
        requireFuture(link);
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
        requireFuture(link);
        if (StringUtils.isNotBlank(link.getPassword())) {
            link.setPasswordHash(EruptShareService.digest(link.getCode(), link.getPassword()));
        }
        link.setPassword(null);
        if (null == link.getEnabled()) link.setEnabled(true);
    }

    // The target column names what the link opens: a model by its display name, a page by its menu
    // name; one that has since gone still names itself by the stored value
    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String type = LambdaSee.field(EruptShareLink::getType);
        String target = LambdaSee.field(EruptShareLink::getTarget);
        String targetName = LambdaSee.field(EruptShareLink::getTargetName);
        for (Map<String, Object> row : list) {
            Object value = row.get(target);
            if (null == value) continue;
            if (EruptShareLink.TYPE_FORM.equals(row.get(type))) {
                EruptModel model = EruptCoreService.getErupt(value.toString());
                row.put(targetName, null == model ? value : I18nTranslate.$translate(model.getErupt().name()));
            } else {
                EruptMenu menu = eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, value.toString()).one();
                row.put(targetName, null == menu ? value : menu.getName());
            }
        }
    }

    // A link that is already over is a mistake in the form, not a link
    private static void requireFuture(EruptShareLink link) {
        if (null != link.getExpireTime() && link.getExpireTime().before(new Date())) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("Expire time must be in the future"));
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
