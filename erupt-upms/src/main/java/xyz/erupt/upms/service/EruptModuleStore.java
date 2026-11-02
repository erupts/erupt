package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptModuleSeed;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Per-database state of modules in {@code e_upms_module}, in place of the marker files erupt used
 * to keep under {@code .erupt}: those are lost with every new container and differ between the
 * nodes of a cluster, while the database is what every instance shares.
 *
 * @author YuePeng
 */
@Service
public class EruptModuleStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private PlatformTransactionManager transactionManager;

    public EruptModuleSeed find(String module) {
        return eruptDao.lambdaQuery(EruptModuleSeed.class).eq(EruptModuleSeed::getModule, module).one();
    }

    /**
     * The module's row, created if missing in a transaction of its own (a duplicate from a
     * concurrent start is fine) and locked in the caller's, so concurrent starts take turns.
     *
     * @param known the row as already read in this transaction, or null
     */
    public EruptModuleSeed lock(String module, EruptModuleSeed known) {
        if (null != known && entityManager.contains(known)) {
            entityManager.refresh(known, LockModeType.PESSIMISTIC_WRITE);
            return known;
        }
        TransactionTemplate separate = new TransactionTemplate(transactionManager);
        separate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        separate.executeWithoutResult(status -> {
            if (null != this.find(module)) return;
            try {
                EruptModuleSeed seed = new EruptModuleSeed();
                seed.setModule(module);
                entityManager.persist(seed);
                entityManager.flush();
            } catch (RuntimeException e) {
                status.setRollbackOnly();
            }
        });
        return entityManager.createQuery("select s from EruptModuleSeed s where s.module = :module", EruptModuleSeed.class)
                .setParameter("module", module).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }

    /**
     * Runs the task the first time this database sees the module, and never again.
     *
     * @return whether it ran
     */
    @Transactional
    public boolean once(String module, Runnable task) {
        EruptModuleSeed known = this.find(module);
        if (null != known && null != known.getSeededAt()) return false;
        EruptModuleSeed seed = this.lock(module, known);
        if (null != seed.getSeededAt()) return false;
        task.run();
        this.markSeeded(seed);
        return true;
    }

    /** A small value the module keeps per database, created by {@code initial} the first time it is asked for. */
    @Transactional
    public String value(String module, Supplier<String> initial) {
        EruptModuleSeed known = this.find(module);
        if (null != known && null != known.getPayload()) return known.getPayload();
        EruptModuleSeed seed = this.lock(module, known);
        if (null == seed.getPayload()) {
            seed.setPayload(initial.get());
            this.markSeeded(seed);
        }
        return seed.getPayload();
    }

    /**
     * Whether an install from before e_upms_module left the module's {@code .erupt/*.loaded} file.
     * Only believed while the database has users: a marker next to an empty database belongs to a
     * database that is gone, which is what the marker files used to get wrong.
     */
    public boolean legacyMarked(String module) {
        return ProjectUtil.loaded(module) && eruptDao.lambdaQuery(EruptUser.class).count() > 0;
    }

    void markSeeded(EruptModuleSeed seed) {
        seed.setEruptVersion(EruptInformation.getEruptVersion());
        seed.setSeededAt(LocalDateTime.now());
    }

}
