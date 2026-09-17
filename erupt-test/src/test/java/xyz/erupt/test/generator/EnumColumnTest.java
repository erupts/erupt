package xyz.erupt.test.generator;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.model.GeneratorField;
import xyz.erupt.test.EruptApplicationTests;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An enum stored as its name must stay free to grow: neither a native enum column type nor a
 * check constraint may pin the values a schema created earlier would accept.
 */
public class EnumColumnTest extends EruptApplicationTests {

    @Resource
    private DataSource dataSource;

    @Test
    public void enumColumnIsPlainVarcharTest() throws Exception {
        assertEquals("character varying", columnType("e_generator_field", "type").toLowerCase());
        assertEquals("character varying", columnType("e_generator_class", "super_class").toLowerCase());
        assertTrue(enumChecks().isEmpty(), enumChecks().toString());
    }

    @Test
    public void declaredLengthIsKeptTest() throws Exception {
        //no declaration: the dialect varchar, wide enough for any constant
        assertEquals("character varying", columnType("e_test_enum_column", "plain").toLowerCase());
        assertEquals(255, columnLength("e_test_enum_column", "plain"));
        //@Column(length) is the author saying how wide, and it is honoured
        assertEquals("character varying", columnType("e_test_enum_column", "sized").toLowerCase());
        assertEquals(32, columnLength("e_test_enum_column", "sized"));
        //@Column(columnDefinition) is the author saying everything, erupt keeps its hands off
        assertEquals("character varying", columnType("e_test_enum_column", "declared").toLowerCase());
        assertEquals(8, columnLength("e_test_enum_column", "declared"));
    }

    @Test
    @Rollback
    @Transactional
    public void everyEnumValueIsAcceptedTest() {
        //every constant has to survive a round trip, including those added after a database was created
        for (GeneratorType type : GeneratorType.values()) {
            GeneratorField field = new GeneratorField();
            field.setFieldName("f" + type.ordinal());
            field.setShowName(type.name());
            field.setSort(type.ordinal());
            field.setType(type);
            eruptDao.persistAndFlush(field);
            eruptDao.getEntityManager().clear();
            assertEquals(type, eruptDao.find(GeneratorField.class, field.getId()).getType());
        }
    }

    private String columnType(String table, String column) throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select data_type from information_schema.columns where table_name = '"
                     + table + "' and column_name = '" + column + "'")) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private int columnLength(String table, String column) throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select character_maximum_length from information_schema.columns where table_name = '"
                     + table + "' and column_name = '" + column + "'")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private List<String> enumChecks() throws Exception {
        List<String> checks = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select check_clause from information_schema.check_constraints")) {
            while (rs.next()) {
                String clause = String.valueOf(rs.getString(1));
                if (clause.contains("'INPUT'") || clause.contains("'BASE_MODEL'")) checks.add(clause);
            }
        }
        return checks;
    }

}
