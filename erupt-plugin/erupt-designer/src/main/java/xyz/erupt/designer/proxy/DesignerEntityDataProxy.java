package xyz.erupt.designer.proxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.designer.model.DesignerEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import xyz.erupt.designer.store.DesignerStore;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2026-06-12
 */
@Service
public class DesignerEntityDataProxy implements DataProxy<DesignerEntity> {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DesignerStore designerStore;

    @Override
    public void beforeAdd(DesignerEntity entity) {
        if (!IDENTIFIER.matcher(entity.getClassName()).matches()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("designer.invalid_class_name") + ": " + entity.getClassName());
        }
        // name taken either by a registered erupt or another design row → one check, one message
        if (null != EruptCoreService.getErupt(entity.getClassName())
                || eruptDao.lambdaQuery(DesignerEntity.class)
                .eq(DesignerEntity::getClassName, entity.getClassName()).count() > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("designer.class_name_exists") + ": " + entity.getClassName());
        }
        entity.setUpdateTime(new Date());
    }


    @Override
    public void afterDelete(DesignerEntity entity) {
        EruptCoreService.unregisterErupt(entity.getClassName());
        // dropping the table is irreversible and the store is outside the JPA transaction,
        // so wait for the commit rather than destroying data a rollback would have kept
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    designerStore.dropTable(entity.getClassName());
                }
            });
        } else {
            designerStore.dropTable(entity.getClassName());
        }
    }

}
