package xyz.erupt.test.core;

import org.hibernate.annotations.Formula;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.support.CommentIntegrator;

import java.util.List;

import static org.mockito.Mockito.*;

class CommentIntegratorFormulaTest {

    @Test
    void skipsFormulaBackedFieldsBeforeReadingColumns() {
        Metadata metadata = mock(Metadata.class);
        PersistentClass entity = mock(PersistentClass.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);
        Table table = mock(Table.class);
        I18nTranslate translator = mock(I18nTranslate.class);

        when(metadata.getEntityBindings()).thenReturn(List.of(entity));
        when(entity.getMappedClass()).thenAnswer(invocation -> FormulaEntity.class);
        when(entity.getTable()).thenReturn(table);
        when(entity.getIdentifierProperty()).thenReturn(null);
        when(entity.getProperties()).thenReturn(List.of(property));

        when(property.getName()).thenReturn("statusLabel");
        when(entity.getProperty("statusLabel")).thenReturn(property);
        when(property.getValue()).thenReturn(value);
        when(value.hasFormula()).thenReturn(true);

        when(translator.translate(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentIntegrator integrator = new CommentIntegrator(translator);

        integrator.integrate(
                metadata,
                null,
                (SessionFactoryServiceRegistry) null
        );

        verify(value, never()).getColumns();
    }

    @Erupt(name = "Formula test")
    static class FormulaEntity {

        @EruptField
        private Long id;

        @Formula("upper(status)")
        @EruptField(views = @View(title = "Status label"))
        private String statusLabel;
    }
}
