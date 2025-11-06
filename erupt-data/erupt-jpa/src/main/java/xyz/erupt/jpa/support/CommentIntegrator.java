package xyz.erupt.jpa.support;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * code from : '<a href="https://github.com/elyar-adil/JpaCommentAnnotation">...</a>'
 *
 * @author YuePeng
 * date 2022/8/3 21:42
 */
@Slf4j
@Component
@AllArgsConstructor
public class CommentIntegrator implements Integrator {

    private final I18nTranslate i18nTranslate;


    /**
     * Perform comment integration.
     *
     * @param metadata        The "compiled" representation of the mapping information
     * @param sessionFactory  The session factory being created
     * @param serviceRegistry The session factory's service registry
     */
    @Override
    public void integrate(@NonNull Metadata metadata, @NonNull SessionFactoryImplementor sessionFactory,@NonNull SessionFactoryServiceRegistry serviceRegistry) {
        this.processComment(metadata);
    }

    /**
     * Not used.
     *
     * @param sessionFactoryImplementor     The session factory being closed.
     * @param sessionFactoryServiceRegistry That session factory's service registry
     */
    @Override
    public void disintegrate(@NonNull SessionFactoryImplementor sessionFactoryImplementor,@NonNull SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
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
                Erupt erupt = clazz.getAnnotation(Erupt.class);
                persistentClass.getTable().setComment(i18nTranslate.translate(erupt.name()));
                Optional.ofNullable(persistentClass.getIdentifierProperty()).ifPresent(it -> {
                    this.fieldComment(persistentClass, it.getName());
                });
                for (Property p : persistentClass.getProperties()) {
                    this.fieldComment(persistentClass, p.getName());
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
                    && !field.isAnnotationPresent(ElementCollection.class)
            ) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                String comment = eruptField.edit().title();
                if (StringUtils.isBlank(comment) && eruptField.views().length > 0) {
                    comment = eruptField.views()[0].title();
                }
                if (StringUtils.isNotBlank(comment)) {
                    String sqlColumnName = persistentClass.getProperty(columnName).getValue().getColumns().iterator().next().getText();
                    for (Column column : persistentClass.getTable().getColumns()) {
                        if (sqlColumnName.equalsIgnoreCase(column.getName())) {
                            column.setComment(i18nTranslate.translate(comment));
                            break;
                        }
                    }
                }
            }
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
