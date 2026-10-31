package xyz.erupt.jpa.support;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.CheckConstraint;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xyz.erupt.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Hibernate writes the values an enum accepts into the create statement: a native {@code enum(...)}
 * column type where the dialect has one, elsewhere a check constraint on the names. Schema update never
 * widens either of them, so a constant added to an enum later would be rejected by every database created
 * before it existed. Dropping the check and storing the name in a plain varchar keeps the enum free to grow.
 * <p>
 * Erupt entities store enums by name, {@code @Enumerated(EnumType.STRING)}; an enum left to its ordinal
 * keeps the plain JPA mapping and is only pointed out.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Slf4j
@Component
public class EnumColumnIntegrator implements Integrator {

    //room for an enum constant when the field does not say how much it needs
    private static final long DEFAULT_LENGTH = 255L;

    //hibernate attaches the check while binding, before any integrator runs, and offers no way to take it back
    private static final Field CHECK_CONSTRAINTS = ReflectUtil.findClassField(Column.class, "checkConstraints");

    @Override
    public void integrate(@NonNull Metadata metadata, @NonNull BootstrapContext bootstrapContext,
                          @NonNull SessionFactoryImplementor sessionFactory) {
        DdlTypeRegistry ddlTypes = metadata.getDatabase().getTypeConfiguration().getDdlTypeRegistry();
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            for (Property property : persistentClass.getProperties()) {
                Field field = ReflectUtil.findClassField(persistentClass.getMappedClass(), property.getName());
                if (null == field || !field.getType().isEnum()) continue;
                Enumerated enumerated = field.getAnnotation(Enumerated.class);
                if (null == enumerated || enumerated.value() != EnumType.STRING) {
                    log.warn("enum column {}.{} is stored as an ordinal, mark it @Enumerated(EnumType.STRING) so the enum can grow",
                            persistentClass.getEntityName(), property.getName());
                    continue;
                }
                try {
                    for (Column column : property.getValue().getColumns()) {
                        this.relaxColumn(ddlTypes, column);
                    }
                } catch (Exception e) {
                    log.warn("enum column {}.{} kept its generated type", persistentClass.getEntityName(), property.getName(), e);
                }
            }
        }
    }

    /**
     * The check hibernate derives from the constants is anonymous, a named {@code @Check} is the author's
     * and stays.
     */
    @SuppressWarnings("unchecked")
    private void relaxColumn(DdlTypeRegistry ddlTypes, Column column) throws IllegalAccessException {
        //an explicit columnDefinition is the author's own decision, only a generated type is replaced
        if (null != column.getSqlType()) return;
        ((List<CheckConstraint>) CHECK_CONSTRAINTS.get(column)).removeIf(CheckConstraint::isAnonymous);
        //the dialect spells the varchar, the field says how wide it has to be
        Size size = Size.length(null == column.getLength() ? DEFAULT_LENGTH : column.getLength());
        column.setSqlType(ddlTypes.getTypeName(SqlTypes.VARCHAR, size, null));
    }

    @Override
    public void disintegrate(@NonNull SessionFactoryImplementor sessionFactoryImplementor,
                             @NonNull SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }

}
