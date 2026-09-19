package xyz.erupt.generator.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.generator.base.ChoiceComment;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.base.Naming;
import xyz.erupt.generator.base.SuperModel;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.model.GeneratorField;
import xyz.erupt.generator.model.input.DbImportModal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads the schema of any datasource registered in the context and turns every
 * selected table into an erupt model definition.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Service
@RequiredArgsConstructor
public class DbIntrospectService {

    //jdbc exposes comments through REMARKS, mysql only fills them from information_schema
    private static final String MYSQL_TABLE_COMMENT = "select table_name, table_comment from information_schema.tables where table_schema = ?";

    private static final String MYSQL_COLUMN_COMMENT = "select table_name, column_name, column_comment from information_schema.columns where table_schema = ?";

    //a namespace is either a catalog or a schema, which one depends on the database
    private static final String CATALOG_PREFIX = "C:";

    private static final String SCHEMA_PREFIX = "S:";

    private static final Set<String> SYSTEM_NAMESPACES = Set.of("information_schema", "performance_schema",
            "mysql", "sys", "pg_catalog", "pg_toast", "sysibm", "sysibmadm", "sysstat", "syscat");

    private static final String PRIMARY_DATA_SOURCE = "dataSource";

    //columns a human would recognise a row by, in the order they are worth trying
    private static final List<String> LABEL_CANDIDATES = List.of("name", "title", "label", "code",
            "nick_name", "full_name", "user_name", "account", "no");

    //a column title longer than this comes from a comment that explains rather than names
    private static final int TITLE_LENGTH = 30;

    //beyond this a column is a text blob, its declared size means nothing
    private static final int MAX_LENGTH = 65535;

    //a suggestion list longer than this stops being a suggestion
    private static final int MAX_COLUMN_TAGS = 300;

    private final Map<String, DataSource> dataSources;

    public List<VLModel> dataSources() {
        List<VLModel> vls = new ArrayList<>();
        dataSources.keySet().stream().sorted().forEach(name -> vls.add(new VLModel(name, name)));
        return vls;
    }

    @SneakyThrows
    public List<VLModel> namespaces(String dataSource) {
        try (Connection conn = dataSources(dataSource).getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<VLModel> vls = new ArrayList<>();
            List<String> catalogs = names(metaData.getCatalogs(), "TABLE_CAT");
            //a single catalog is the connection itself and carries no choice
            if (catalogs.size() > 1) {
                catalogs.forEach(it -> vls.add(new VLModel(CATALOG_PREFIX + it, it)));
            }
            names(metaData.getSchemas(), "TABLE_SCHEM").forEach(it -> vls.add(new VLModel(SCHEMA_PREFIX + it, it)));
            if (vls.isEmpty()) {
                String current = currentNamespace(conn);
                vls.add(new VLModel(current, current.substring(2)));
            }
            return vls;
        }
    }

    @SneakyThrows
    public List<VLModel> tables(String dataSource, String namespace) {
        try (Connection conn = dataSources(dataSource).getConnection()) {
            Target target = Target.of(conn, namespace);
            Map<String, String> comments = comments(conn, target, MYSQL_TABLE_COMMENT, false);
            List<VLModel> vls = new ArrayList<>();
            try (ResultSet rs = conn.getMetaData().getTables(target.catalog(), target.schema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    String comment = comment(comments.get(table), rs.getString("REMARKS"));
                    vls.add(new VLModel(table, null == comment ? table : table + " - " + comment));
                }
            }
            return vls;
        }
    }

    /**
     * Columns of the tables picked so far, the ones several tables share come first:
     * an audit column is exactly the one that shows up everywhere.
     */
    @SneakyThrows
    public List<String> columns(DbImportModal modal) {
        if (null == modal.getTables() || modal.getTables().isEmpty()) return Collections.emptyList();
        try (Connection conn = dataSources(modal.getDataSource()).getConnection()) {
            Target target = Target.of(conn, modal.getNamespace());
            SuperModel superModel = null == modal.getSuperClass() ? SuperModel.NONE : modal.getSuperClass();
            Map<String, Integer> counts = new HashMap<>();
            for (String table : modal.getTables()) {
                try (ResultSet rs = conn.getMetaData().getColumns(target.catalog(), target.schema(), table, "%")) {
                    while (rs.next()) {
                        String column = rs.getString("COLUMN_NAME");
                        //a column the parent class already declares never reaches the entity
                        if (!superModel.inherit(column)) counts.merge(column, 1, Integer::sum);
                    }
                }
            }
            return counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .limit(MAX_COLUMN_TAGS).map(Map.Entry::getKey).toList();
        }
    }

    @SneakyThrows
    public String defaultNamespace(String dataSource) {
        try (Connection conn = dataSources(dataSource).getConnection()) {
            return currentNamespace(conn);
        }
    }

    public String defaultDataSource() {
        return dataSources.containsKey(PRIMARY_DATA_SOURCE) ? PRIMARY_DATA_SOURCE : dataSources.keySet().iterator().next();
    }

    @SneakyThrows
    public List<GeneratorClass> introspect(DbImportModal modal) {
        try (Connection conn = dataSources(modal.getDataSource()).getConnection()) {
            Target target = Target.of(conn, modal.getNamespace());
            Map<String, String> tableComments = comments(conn, target, MYSQL_TABLE_COMMENT, false);
            Map<String, String> columnComments = comments(conn, target, MYSQL_COLUMN_COMMENT, true);
            List<String> ignoreColumns = split(modal.getIgnoreColumns());
            List<GeneratorClass> classes = new ArrayList<>();
            List<String> unsupported = new ArrayList<>();
            //a referenced table is read once however many foreign keys point at it
            Map<String, Link> links = new HashMap<>();
            for (String table : modal.getTables()) {
                String primaryKey = primaryKey(conn, target, table);
                //erupt addresses a row by a single primary key, a composite or missing one cannot be mapped
                if (null == primaryKey) {
                    unsupported.add(table);
                    continue;
                }
                classes.add(readTable(conn, target, table, primaryKey, modal, ignoreColumns, tableComments, columnComments, links));
            }
            if (!unsupported.isEmpty()) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("generator.unsupported_primary_key")
                        + " → " + String.join(", ", unsupported));
            }
            return classes;
        }
    }

    /**
     * Where a reference points: the field holding the id and the field worth showing.
     */
    @SneakyThrows
    private Link link(Connection conn, Target target, String table) {
        String primaryKey = primaryKey(conn, target, table);
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = conn.getMetaData().getColumns(target.catalog(), target.schema(), table, "%")) {
            while (rs.next()) columns.add(rs.getString("COLUMN_NAME"));
        }
        String label = null;
        for (String candidate : LABEL_CANDIDATES) {
            label = columns.stream().filter(it -> it.equalsIgnoreCase(candidate)).findFirst().orElse(null);
            if (null != label) break;
        }
        if (null == label) {
            //nothing reads like a name, the first column that is not the key at least says something
            label = columns.stream().filter(it -> !it.equals(primaryKey)).findFirst().orElse(primaryKey);
        }
        return new Link(table, Naming.fieldName(null == primaryKey ? "id" : primaryKey),
                Naming.fieldName(null == label ? "id" : label));
    }

    private String primaryKey(Connection conn, Target target, String table) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        try (ResultSet rs = conn.getMetaData().getPrimaryKeys(target.catalog(), target.schema(), table)) {
            while (rs.next()) primaryKeys.add(rs.getString("COLUMN_NAME"));
        }
        return 1 == primaryKeys.size() ? primaryKeys.get(0) : null;
    }

    private GeneratorClass readTable(Connection conn, Target target, String table, String primaryKey, DbImportModal modal,
                                     List<String> ignoreColumns, Map<String, String> tableComments,
                                     Map<String, String> columnComments, Map<String, Link> links) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        Map<String, Link> foreignKeys = new HashMap<>();
        try (ResultSet rs = metaData.getImportedKeys(target.catalog(), target.schema(), table)) {
            while (rs.next()) {
                String linkTable = rs.getString("PKTABLE_NAME");
                foreignKeys.put(rs.getString("FKCOLUMN_NAME"), links.computeIfAbsent(linkTable,
                        it -> link(conn, target, it)));
            }
        }
        GeneratorClass clazz = new GeneratorClass();
        clazz.setTableName(table);
        clazz.setPackageName(modal.getPackageName());
        clazz.setClassName(Naming.className(table));
        //an inherited id only fits a table whose primary key is called id
        clazz.setSuperClass("id".equalsIgnoreCase(primaryKey) ? modal.getSuperClass() : SuperModel.NONE);
        String comment = comment(tableComments.get(table), null);
        clazz.setName(null == comment ? Naming.title(clazz.getClassName()) : title(comment));
        clazz.setRemark(comment);
        Set<GeneratorField> fields = new LinkedHashSet<>();
        int sort = 0;
        try (ResultSet rs = metaData.getColumns(target.catalog(), target.schema(), table, "%")) {
            while (rs.next()) {
                String column = rs.getString("COLUMN_NAME");
                if (clazz.getSuperClass().inherit(column) || ignore(column, ignoreColumns)) continue;
                String columnComment = comment(columnComments.get(table + "." + column), rs.getString("REMARKS"));
                GeneratorField field = readColumn(rs, column, columnComment, primaryKey, foreignKeys, modal, sort += 10);
                fields.add(field);
            }
        }
        clazz.setFields(fields);
        return clazz;
    }

    private GeneratorField readColumn(ResultSet rs, String column, String comment, String primaryKey,
                                      Map<String, Link> foreignKeys, DbImportModal modal, int sort) throws SQLException {
        int jdbcType = rs.getInt("DATA_TYPE");
        int size = rs.getInt("COLUMN_SIZE");
        int scale = rs.getInt("DECIMAL_DIGITS");
        Link link = foreignKeys.get(column);
        GeneratorField field = new GeneratorField();
        field.setSort(sort);
        field.setNotNull(DatabaseMetaData.columnNoNulls == rs.getInt("NULLABLE"));
        field.setPrimaryKey(column.equals(primaryKey));
        field.setAutoIncrement(autoIncrement(rs));
        if (null == link) {
            GeneratorType type = GeneratorType.of(jdbcType, rs.getString("TYPE_NAME"), size, column);
            field.setFieldName(Naming.fieldName(column));
            field.setType(type);
            field.setJavaType(GeneratorType.javaType(jdbcType, scale));
            if (String.class.getSimpleName().equals(type.getType()) && size > 0 && size <= MAX_LENGTH) {
                field.setLength(size);
            }
            //a code column explains its values in the comment, that explanation is the choice list
            ChoiceComment.Choice choice = GeneratorType.NUMBER == type || GeneratorType.INPUT == type
                    ? ChoiceComment.parse(comment) : null;
            if (null != choice) {
                field.setType(GeneratorType.CHOICE);
                field.setTypeCode(choice.code());
                if (!choice.title().isEmpty()) comment = choice.title();
                if (GeneratorType.NUMBER == type && null == field.getJavaType()) {
                    field.setJavaType(Integer.class.getSimpleName());
                }
            }
        } else {
            //a foreign key reads better as the entity it points at
            field.setFieldName(Naming.fieldName(column.replaceAll("(?i)_?id$", "")));
            field.setType(GeneratorType.REFERENCE_TABLE);
            field.setLinkClass(Naming.className(link.table()));
            //the referenced entity rarely has both an id and a name field, ask the table what it has
            field.setTypeCode("referenceTableType = @ReferenceTableType(id = \"" + link.id() + "\", label = \"" + link.label() + "\")");
        }
        //the title follows the field, a foreign key is named after the entity and not after its column
        field.setShowName(null == comment ? Naming.title(field.getFieldName()) : title(comment));
        if (!Naming.humpToLine(field.getFieldName()).equals(column)) field.setColumnName(column);
        field.setQuery(field.getType().searchable() || null != link);
        field.setSortable(false);
        field.setIsShow(true);
        return field;
    }

    /**
     * mysql keeps comments out of the jdbc metadata unless the connection was opened
     * with useInformationSchema, every other database answers through REMARKS.
     */
    private Map<String, String> comments(Connection conn, Target target, String sql, boolean column) throws SQLException {
        String product = conn.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        if (!product.contains("mysql") && !product.contains("mariadb")) return Collections.emptyMap();
        Map<String, String> comments = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, null == target.catalog() ? conn.getCatalog() : target.catalog());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.put(column ? rs.getString(1) + "." + rs.getString(2) : rs.getString(1),
                            rs.getString(column ? 3 : 2));
                }
            }
        }
        return comments;
    }

    private DataSource dataSources(String name) {
        DataSource dataSource = null == name ? null : dataSources.get(name);
        return null == dataSource ? dataSources.get(defaultDataSource()) : dataSource;
    }

    private static List<String> names(ResultSet rs, String column) throws SQLException {
        List<String> names = new ArrayList<>();
        try (rs) {
            while (rs.next()) {
                String name = rs.getString(column);
                if (null != name && !SYSTEM_NAMESPACES.contains(name.toLowerCase(Locale.ROOT))) names.add(name);
            }
        }
        return names;
    }

    private static String currentNamespace(Connection conn) throws SQLException {
        String schema = schema(conn);
        return null == schema || schema.isEmpty() ? CATALOG_PREFIX + conn.getCatalog() : SCHEMA_PREFIX + schema;
    }

    private static List<String> split(String text) {
        if (null == text || text.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(text.split(",")).map(String::trim).filter(it -> !it.isEmpty()).toList();
    }

    //glob matching without a regular expression, the patterns come from a text field
    private static boolean ignore(String column, List<String> patterns) {
        String name = column.toLowerCase(Locale.ROOT);
        for (String pattern : patterns) {
            String glob = pattern.toLowerCase(Locale.ROOT);
            boolean match = glob.startsWith("*") ? name.endsWith(glob.substring(1))
                    : glob.endsWith("*") ? name.startsWith(glob.substring(0, glob.length() - 1))
                    : name.equals(glob);
            if (match) return true;
        }
        return false;
    }

    private static boolean autoIncrement(ResultSet rs) {
        try {
            return "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"));
        } catch (SQLException e) {
            return false;
        }
    }

    private static String schema(Connection conn) {
        try {
            return conn.getSchema();
        } catch (SQLException | AbstractMethodError e) {
            return null;
        }
    }

    private static String comment(String first, String second) {
        String comment = null == first || first.isEmpty() ? second : first;
        return null == comment || comment.trim().isEmpty() ? null : comment.trim();
    }

    //a comment often documents the column instead of naming it, only its head is a title
    private static String title(String comment) {
        String title = comment.split("\n")[0].trim();
        return title.length() > TITLE_LENGTH ? title.substring(0, TITLE_LENGTH) : title;
    }

    //what a foreign key points at, resolved once per referenced table
    private record Link(String table, String id, String label) {
    }

    //the pair of jdbc metadata arguments a namespace resolves to
    private record Target(String catalog, String schema) {

        static Target of(Connection conn, String namespace) throws SQLException {
            String value = null == namespace || namespace.isEmpty() ? currentNamespace(conn) : namespace;
            return value.startsWith(SCHEMA_PREFIX)
                    ? new Target(conn.getCatalog(), value.substring(SCHEMA_PREFIX.length()))
                    : new Target(value.substring(CATALOG_PREFIX.length()), null);
        }

    }

}
