package xyz.erupt.jpa.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.util.ReflectUtil;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Optional;

/**
 * code from : 'https://github.com/elyar-adil/JpaCommentAnnotation'
 *
 * @author YuePeng
 * date 2022/8/3 21:42
 */
@Slf4j
public class CommentIntegrator implements Integrator {

    /**
     * Perform comment integration.
     *
     * @param metadata        The "compiled" representation of the mapping information
     * @param sessionFactory  The session factory being created
     * @param serviceRegistry The session factory's service registry
     */
    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        this.processComment(metadata);
    }

    /**
     * Not used.
     *
     * @param sessionFactoryImplementor     The session factory being closed.
     * @param sessionFactoryServiceRegistry That session factory's service registry
     */
    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }

    /**
     * Process comment annotation.
     *
     * @param metadata process annotation of this {@code Metadata}.
     */
    private void processComment(Metadata metadata) {
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            // Process the Comment annotation is applied to Class
            Class<?> clazz = persistentClass.getMappedClass();
            if (clazz.isAnnotationPresent(Erupt.class)) {
                Erupt comment = clazz.getAnnotation(Erupt.class);
                persistentClass.getTable().setComment(comment.name());
                Optional.ofNullable(persistentClass.getIdentifierProperty()).ifPresent(it -> {
                    this.fieldComment(persistentClass, it.getName());
                });
                Iterator<Property> iterator = persistentClass.getPropertyIterator();
                while (iterator.hasNext()) {
                    this.fieldComment(persistentClass, iterator.next().getName());
                }
            }
        }
    }

    /**
     * Process @{code comment} annotation of field.
     *
     * @param persistentClass Hibernate {@code PersistentClass}
     * @param columnName      name of field
     */
    private void fieldComment(PersistentClass persistentClass, String columnName) {
        try {
            Field field = ReflectUtil.findClassField(persistentClass.getMappedClass(), columnName);
            if (null == field) return;
            if (field.isAnnotationPresent(EruptField.class)
                    && !field.isAnnotationPresent(OneToOne.class)
                    && !field.isAnnotationPresent(OneToMany.class)
                    && !field.isAnnotationPresent(ManyToMany.class)
            ) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                String comment = eruptField.edit().title();
                if (StringUtils.isBlank(comment) && eruptField.views().length > 0) {
                    comment = eruptField.views()[0].title();
                }
                if (StringUtils.isNotBlank(comment)) {
                    String sqlColumnName = persistentClass.getProperty(columnName).getValue().getColumnIterator().next().getText();
                    Iterator<Column> columnIterator = persistentClass.getTable().getColumnIterator();
                    while (columnIterator.hasNext()) {
                        Column column = columnIterator.next();
                        if (sqlColumnName.equalsIgnoreCase(column.getName())) {
                            column.setComment(comment);
                            break;
                        }
                    }
                }
            }
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

}
