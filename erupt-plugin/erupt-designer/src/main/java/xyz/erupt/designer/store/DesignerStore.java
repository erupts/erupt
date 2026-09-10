package xyz.erupt.designer.store;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.designer.config.EruptDesignerProp;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Embedded SQLite store for designer data: one file, one real table per published design.
 * <p>
 * Real columns let the JDBC layer push filtering / sorting / paging down as SQL with a single
 * dialect, independent of the main database vendor. Schema changes are additive only (columns are
 * added, never dropped or retyped) and idempotent, so a publish that fails half-way can simply be
 * re-run. SQLite's type affinity makes field type changes a no-op at the storage level.
 *
 * @author YuePeng
 * date 2026-09-09
 */
@Slf4j
@Component
public class DesignerStore {

    public static final String TABLE_PREFIX = "d_";

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    // SQLite keywords that would break unquoted SQL when used as a column name
    private static final Set<String> RESERVED = Set.of(
            "add", "all", "alter", "and", "as", "asc", "between", "by", "case", "check", "collate", "column",
            "commit", "constraint", "create", "cross", "default", "delete", "desc", "distinct", "drop", "else",
            "end", "escape", "except", "exists", "foreign", "from", "full", "group", "having", "in", "index",
            "inner", "insert", "intersect", "into", "is", "isnull", "join", "left", "like", "limit", "natural",
            "not", "notnull", "null", "offset", "on", "or", "order", "outer", "primary", "references", "right",
            "rollback", "select", "set", "table", "then", "to", "transaction", "union", "unique", "update",
            "using", "values", "when", "where", "with", "without");

    @Resource
    private EruptDesignerProp prop;

    private HikariDataSource dataSource;

    @Getter
    private NamedParameterJdbcTemplate template;

    @PostConstruct
    @SneakyThrows
    public void init() {
        Path path = Paths.get(prop.getDbPath()).toAbsolutePath();
        if (null != path.getParent()) Files.createDirectories(path.getParent());
        HikariConfig config = new HikariConfig();
        config.setPoolName("erupt-designer");
        config.setJdbcUrl("jdbc:sqlite:" + path);
        config.setMaximumPoolSize(prop.getMaxPoolSize());
        // driver properties: concurrent readers with a single writer, wait instead of failing on lock,
        // dates as readable ISO text so they sort and compare lexicographically
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("busy_timeout", "5000");
        config.addDataSourceProperty("date_class", "TEXT");
        config.addDataSourceProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
        this.dataSource = new HikariDataSource(config);
        this.template = new NamedParameterJdbcTemplate(dataSource);
        log.info("Designer store → {}", path);
    }

    @PreDestroy
    public void close() {
        if (null != dataSource) dataSource.close();
    }

    public static String tableName(String className) {
        return TABLE_PREFIX + className.toLowerCase();
    }

    // create the table when missing, then add any column the design declares but the table lacks
    public void ensureTable(EruptModel model) {
        String pk = model.getErupt().primaryKeyCol();
        String table = tableName(model.getEruptName());
        template.getJdbcTemplate().execute("create table if not exists " + table
                + " (" + pk + " integer primary key autoincrement)");
        Set<String> existing = this.columns(table);
        for (EruptFieldModel field : model.getEruptFieldModels()) {
            String column = this.legalColumn(field.getFieldName());
            if (existing.contains(column.toLowerCase())) continue;
            template.getJdbcTemplate().execute("alter table " + table + " add column " + column + " "
                    + this.columnType(field.getField().getType()));
        }
    }

    /**
     * Move an existing column so a renamed field keeps the data it already holds. Returns false
     * without touching the table when the rename cannot be applied safely: the table or the source
     * column does not exist yet, or the target name is already taken by another column (a column
     * left behind by a deleted field, say). Skipping is deliberate — the source column and its data
     * survive under the old name and the caller reports it, which beats destroying either side.
     */
    public boolean renameColumn(String className, String from, String to) {
        String table = tableName(className);
        Set<String> columns = this.columns(table);
        if (!columns.contains(from.toLowerCase()) || columns.contains(to.toLowerCase())) {
            return false;
        }
        template.getJdbcTemplate().execute("alter table " + table + " rename column "
                + this.legalColumn(from) + " to " + this.legalColumn(to));
        return true;
    }

    public void dropTable(String className) {
        template.getJdbcTemplate().execute("drop table if exists " + tableName(className));
    }

    private Set<String> columns(String table) {
        return template.getJdbcTemplate().queryForList("pragma table_info(" + table + ")").stream()
                .map(it -> String.valueOf(it.get("name")).toLowerCase()).collect(Collectors.toSet());
    }

    private String legalColumn(String name) {
        if (null == name || !IDENTIFIER.matcher(name).matches() || RESERVED.contains(name.toLowerCase())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("designer.invalid_field_name") + ": " + name);
        }
        return name;
    }

    private String columnType(Class<?> type) {
        if (type == Integer.class || type == Long.class || type == Short.class || type == Boolean.class) return "integer";
        if (type == Double.class || type == Float.class) return "real";
        if (type == BigDecimal.class) return "numeric";
        return "text";
    }

}
