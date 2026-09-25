package xyz.erupt.jpa.support;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
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

/**
 * Hibernate writes the values an enum accepts into the create statement, as a native
 * {@code enum(...)} column type and as a check constraint. Schema update never widens either
 * of them, so a constant added to an enum later would be rejected by every database created
 * before it existed. Storing the name in a plain varchar keeps the enum free to grow.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Slf4j
@Component
public class EnumColumnIntegrator implements Integrator {

    //room for an enum constant when the field does not say how much it needs
    private static final long DEFAULT_LENGTH = 255L;

    @Override
    public void integrate(@NonNull Metadata metadata, @NonNull SessionFactoryImplementor sessionFactory,
                          @NonNull SessionFactoryServiceRegistry serviceRegistry) {
        DdlTypeRegistry ddlTypes = metadata.getDatabase().getTypeConfiguration().getDdlTypeRegistry();
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            for (Property property : persistentClass.getProperties()) {
                Field field = ReflectUtil.findClassField(persistentClass.getMappedClass(), property.getName());
                if (null == field || !field.getType().isEnum()) continue;
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
     * A declared column type also drops the check constraint hibernate would have generated
     * beside it, so stating the type is all it takes to unpin the values.
     */
    private void relaxColumn(DdlTypeRegistry ddlTypes, Column column) {
        //an explicit columnDefinition is the author's own decision, only a generated type is replaced
        if (null != column.getSqlType()) return;
        //the dialect spells the varchar, the field says how wide it has to be
        Size size = Size.length(null == column.getLength() ? DEFAULT_LENGTH : column.getLength());
        column.setSqlType(ddlTypes.getTypeName(SqlTypes.VARCHAR, size));
    }

    @Override
    public void disintegrate(@NonNull SessionFactoryImplementor sessionFactoryImplementor,
                             @NonNull SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }

}
