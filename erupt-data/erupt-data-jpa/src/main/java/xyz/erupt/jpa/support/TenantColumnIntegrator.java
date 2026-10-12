package xyz.erupt.jpa.support;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.TenantId;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.stereotype.Component;
import xyz.erupt.core.util.ReflectUtil;

import java.lang.reflect.Field;

/**
 * Hibernate restricts every query on an entity with a {@link TenantId} attribute through a filter
 * whose condition names the column <em>unquoted</em>, while erupt quotes every identifier it
 * creates. Databases that fold unquoted names (Oracle and H2 upper-case them, PostgreSQL
 * lower-cases them) then cannot match the filter against the quoted column. Leaving that one
 * column unquoted makes the DDL and the filter agree on every database.
 * <p>
 * The mapping model offers no way to drop the quoting once global quoting applied it, so the
 * flag is cleared reflectively; the integrator logs and leaves the column alone if Hibernate
 * ever renames the field.
 *
 * @author YuePeng
 */
@Slf4j
@Component
public class TenantColumnIntegrator implements Integrator {

    private static final Field QUOTED = quotedField();

    @Override
    public void integrate(@NonNull Metadata metadata, @NonNull SessionFactoryImplementor sessionFactory,
                          @NonNull SessionFactoryServiceRegistry serviceRegistry) {
        if (null == QUOTED) return;
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            for (Property property : persistentClass.getProperties()) {
                Field field = ReflectUtil.findClassField(persistentClass.getMappedClass(), property.getName());
                if (null == field || !field.isAnnotationPresent(TenantId.class)) continue;
                for (Column column : property.getValue().getColumns()) {
                    this.unquote(column);
                }
            }
        }
    }

    private void unquote(Column column) {
        try {
            QUOTED.set(column, false);
        } catch (IllegalAccessException e) {
            log.warn("tenant column {} kept its quoting, tenant filters may fail on case-folding databases", column.getName(), e);
        }
    }

    private static Field quotedField() {
        try {
            Field field = Column.class.getDeclaredField("quoted");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException e) {
            log.warn("Hibernate Column no longer exposes 'quoted'; tenant columns keep their quoting", e);
            return null;
        }
    }

    @Override
    public void disintegrate(@NonNull SessionFactoryImplementor sessionFactoryImplementor,
                             @NonNull SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }
}
