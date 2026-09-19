package xyz.erupt.test.generator;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.generator.base.ChoiceComment;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.base.SuperModel;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.model.GeneratorField;
import xyz.erupt.generator.handler.CodeDownloadHandler;
import xyz.erupt.generator.handler.DbImportHandler;
import xyz.erupt.generator.handler.CodePreviewHandler;
import xyz.erupt.generator.model.input.DbImportModal;
import xyz.erupt.generator.service.CodeRender;
import xyz.erupt.generator.service.DbIntrospectService;
import xyz.erupt.test.EruptApplicationTests;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Introspects real tables through jdbc metadata and renders them as erupt entities.
 */
public class DbIntrospectTest extends EruptApplicationTests {

    @Resource
    private DbIntrospectService dbIntrospectService;

    @Resource
    private DataSource dataSource;

    @Resource
    private CodePreviewHandler codePreviewHandler;

    @Resource
    private CodeDownloadHandler codeDownloadHandler;

    @Resource
    private DbImportHandler dbImportHandler;

    @BeforeEach
    public void createTables() throws Exception {
        execute("create table e_gen_dept (id bigint auto_increment primary key, name varchar(64) not null, remark varchar(200))",
                "create table e_gen_employee (" +
                        "id bigint auto_increment primary key," +
                        "emp_name varchar(64) not null," +
                        "emp_no varchar(32) unique," +
                        "password varchar(64)," +
                        "salary decimal(12,2)," +
                        "status int," +
                        "hired_on timestamp," +
                        "active boolean," +
                        "remark varchar(2000)," +
                        "dept_id bigint," +
                        "constraint fk_gen_employee_dept foreign key (dept_id) references e_gen_dept(id))",
                "comment on column e_gen_employee.status is 'state 0-disabled 1-enabled'",
                "create table e_gen_audit (audit_no varchar(32) primary key, action varchar(64))");
    }

    @AfterEach
    public void dropTables() throws Exception {
        execute("drop table if exists e_gen_employee", "drop table if exists e_gen_dept", "drop table if exists e_gen_audit");
    }

    @Test
    public void tableListTest() {
        assertEquals("e_gen_employee", actualName("e_gen_employee").toLowerCase());
    }

    @Test
    public void columnMappingTest() {
        GeneratorClass clazz = introspect("e_gen_employee");
        //the erupt prefix is the one naming convention the generator knows
        assertEquals("GenEmployee", clazz.getClassName());
        assertEquals(SuperModel.BASE_MODEL, clazz.getSuperClass());
        Map<String, GeneratorField> fields = fields(clazz);
        //the primary key is inherited from BaseModel
        assertFalse(fields.containsKey("id"));
        assertEquals(GeneratorType.INPUT, fields.get("empName").getType());
        assertEquals(64, fields.get("empName").getLength());
        assertTrue(fields.get("empName").getNotNull());
        assertEquals(GeneratorType.PASSWORD, fields.get("password").getType());
        assertEquals(GeneratorType.NUMBER, fields.get("salary").getType());
        assertEquals("BigDecimal", fields.get("salary").getJavaType());
        assertEquals(GeneratorType.DATE_TIME, fields.get("hiredOn").getType());
        assertEquals(GeneratorType.BOOLEAN, fields.get("active").getType());
        assertEquals(GeneratorType.TEXTAREA, fields.get("remark").getType());
        //a foreign key becomes the entity it points at, labelled by a column that exists
        GeneratorField dept = fields.get("dept");
        assertEquals(GeneratorType.REFERENCE_TABLE, dept.getType());
        assertEquals("GenDept", dept.getLinkClass());
        assertEquals("referenceTableType = @ReferenceTableType(id = \"id\", label = \"name\")", dept.getTypeCode());
    }

    @Test
    public void choiceFromCommentTest() {
        //h2 keeps column comments, the values a code column documents become its choice list
        GeneratorField status = fields(introspect("e_gen_employee")).get("status");
        assertEquals(GeneratorType.CHOICE, status.getType());
        assertEquals("Integer", status.getJavaType());
        //the dictionary belongs to the choice list, only its head is the title
        assertEquals("state", status.getShowName());
        assertTrue(status.getTypeCode().contains("@VL(value = \"0\", label = \"disabled\")"), status.getTypeCode());
        assertTrue(status.getTypeCode().contains("@VL(value = \"1\", label = \"enabled\")"), status.getTypeCode());
    }

    @Test
    public void choiceCommentParseTest() {
        assertNotNull(ChoiceComment.parse("1:male 2:female"));
        assertNotNull(ChoiceComment.parse("type 1=normal,2=group"));
        //a single pair is a sentence and a date is not a dictionary
        assertNull(ChoiceComment.parse("status 1-normal"));
        assertNull(ChoiceComment.parse("created at 2020-01-01"));
    }

    @Test
    public void renderTest() {
        DbImportModal modal = modal("e_gen_employee");
        modal.setPackageName("com.example.model");
        String code = CodeRender.render(dbIntrospectService.introspect(modal).get(0));
        assertTrue(code.startsWith("package com.example.model;"), code);
        assertTrue(code.contains("public class GenEmployee extends BaseModel {"), code);
        assertTrue(code.contains("import java.math.BigDecimal;"), code);
        assertTrue(code.contains("@ManyToOne"), code);
        assertTrue(code.contains("private GenDept dept;"), code);
        assertTrue(code.contains("type = EditType.CHOICE"), code);
        //an inherited id is never declared twice
        assertFalse(code.contains("@Id"), code);
    }

    @Test
    public void previewOneClassTest() {
        String expr = codePreviewHandler.exec(List.of(introspect("e_gen_employee")), null, null);
        assertTrue(expr.startsWith("codeDrawer('java', "), expr.substring(0, 40));
        assertTrue(expr.endsWith(", \"GenEmployee.java\")"), expr.substring(expr.length() - 40));
    }

    @Test
    public void downloadOneClassTest() {
        String expr = codeDownloadHandler.exec(List.of(introspect("e_gen_employee")), null, null);
        //a single class travels as the java file itself rather than as an archive
        assertTrue(expr.startsWith("downloadFile(\"GenEmployee.java\", "), expr.substring(0, 40));
        assertTrue(new String(Base64.getDecoder().decode(expr.split("\"")[3]), StandardCharsets.UTF_8)
                .contains("public class GenEmployee"), expr.substring(0, 40));
    }

    @Test
    public void downloadManyClassesTest() throws Exception {
        DbImportModal modal = modal("e_gen_employee");
        modal.getTables().add(actualName("e_gen_dept"));
        modal.setPackageName("com.example.model");
        String expr = codeDownloadHandler.exec(dbIntrospectService.introspect(modal), null, null);
        //several classes are handed over as one archive
        assertTrue(expr.startsWith("downloadFile(\"erupt-code.zip\", "), expr.substring(0, 40));
        List<String> entries = new ArrayList<>();
        byte[] zip = Base64.getDecoder().decode(expr.split("\"")[3]);
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry = in.getNextEntry(); null != entry; entry = in.getNextEntry()) {
                entries.add(entry.getName());
                assertTrue(new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("@Erupt"), entry.getName());
            }
        }
        //the package becomes the path, so the archive unzips over a source tree
        assertTrue(entries.contains("com/example/model/GenEmployee.java"), entries.toString());
        assertTrue(entries.contains("com/example/model/GenDept.java"), entries.toString());
    }

    @Test
    public void foreignPrimaryKeyTest() {
        //a table keyed by something else than id cannot inherit one
        GeneratorClass clazz = introspect("e_gen_audit");
        assertEquals(SuperModel.NONE, clazz.getSuperClass());
        assertTrue(fields(clazz).get("auditNo").getPrimaryKey());
        String code = CodeRender.render(clazz);
        assertTrue(code.contains("primaryKeyCol = \"auditNo\""), code);
        assertTrue(code.contains("@Id"), code);
        assertFalse(code.contains("extends"), code);
    }

    @Test
    public void dataSourceChoiceTest() {
        //the primary datasource is always offered, erupt-designer registers a second one
        assertTrue(dbIntrospectService.dataSources().stream().anyMatch(it -> "dataSource".equals(it.getValue())));
        assertEquals("dataSource", dbIntrospectService.defaultDataSource());
        String namespace = dbIntrospectService.defaultNamespace(null);
        assertNotNull(namespace);
        //the default has to be one of the offered options or the select would open empty
        assertTrue(dbIntrospectService.namespaces(null).stream().anyMatch(it -> namespace.equals(it.getValue())), namespace);
    }

    @Test
    public void columnSuggestionTest() {
        DbImportModal modal = modal("e_gen_employee");
        modal.getTables().add(actualName("e_gen_dept"));
        List<String> columns = dbIntrospectService.columns(modal);
        //id is declared by BaseModel and never reaches the entity
        assertTrue(columns.stream().noneMatch(it -> it.equalsIgnoreCase("id")), columns.toString());
        //remark is the only column both tables share, so it leads the suggestions
        assertEquals("remark", columns.get(0).toLowerCase());
        assertTrue(columns.stream().anyMatch(it -> it.equalsIgnoreCase("emp_name")), columns.toString());
        //nothing picked means nothing to suggest
        assertTrue(dbIntrospectService.columns(new DbImportModal()).isEmpty());
    }

    @Test
    public void formDefaultsTest() {
        DbImportModal modal = dbImportHandler.eruptFormValue(List.of(), new DbImportModal(), null);
        assertEquals("dataSource", modal.getDataSource());
        assertNotNull(modal.getNamespace());
        //the models of this application already say where a generated class belongs
        assertTrue(modal.getPackageName().startsWith("xyz.erupt.test."), modal.getPackageName());
    }

    @Test
    public void ignoreColumnTest() {
        DbImportModal modal = modal("e_gen_employee");
        modal.setIgnoreColumns("password, hired_*");
        Map<String, GeneratorField> fields = fields(dbIntrospectService.introspect(modal).get(0));
        assertFalse(fields.containsKey("password"));
        assertFalse(fields.containsKey("hiredOn"));
        assertTrue(fields.containsKey("empName"));
    }

    @Test
    public void compositePrimaryKeyTest() throws Exception {
        execute("create table e_gen_composite (a bigint not null, b bigint not null, primary key (a, b))");
        try {
            assertThrows(RuntimeException.class, () -> introspect("e_gen_composite"));
        } finally {
            execute("drop table if exists e_gen_composite");
        }
    }

    private GeneratorClass introspect(String table) {
        return dbIntrospectService.introspect(modal(table)).get(0);
    }

    private DbImportModal modal(String table) {
        DbImportModal modal = new DbImportModal();
        modal.setSuperClass(SuperModel.BASE_MODEL);
        //metadata lookups are case sensitive, ask the database how it spells the table
        modal.setTables(new LinkedHashSet<>(List.of(actualName(table))));
        return modal;
    }

    private String actualName(String table) {
        return dbIntrospectService.tables(null, null).stream().map(VLModel::getValue)
                .filter(it -> it.equalsIgnoreCase(table)).findFirst().orElseThrow();
    }

    private Map<String, GeneratorField> fields(GeneratorClass clazz) {
        return clazz.getFields().stream().collect(Collectors.toMap(GeneratorField::getFieldName, Function.identity()));
    }

    private void execute(String... sqls) throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {
            for (String sql : sqls) statement.execute(sql);
        }
    }

}
