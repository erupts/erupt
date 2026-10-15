package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A table behind any JDBC {@link DataSource}. Columns are the model's {@code @EruptField} names (quoted, so case
 * sensitive on PostgreSQL and Oracle; a reference column holds the referenced key); every value is a bound
 * parameter, LIKE is escaped with {@code ESCAPE '!'}. Without a sort, rows come back by primary key ascending.
 * The dialect (identifier quotes and paging) is detected from the connection unless set. Create the table yourself.
 */
public final class JdbcRepository<T> implements Repository<T> {

    /** Identifier quotes and paging of a database. */
    public enum Dialect {
        /** PostgreSQL, H2, SQLite, HSQLDB and other ANSI quoting with {@code LIMIT ? OFFSET ?}. */
        STANDARD("\"", "\"", false),
        MYSQL("`", "`", false),
        /** SQL Server 2012+: {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY}. */
        SQLSERVER("[", "]", true),
        /** Oracle 12c+. */
        ORACLE("\"", "\"", true);

        final String open, close;
        final boolean fetch;

        Dialect(String open, String close, boolean fetch) {
            this.open = open;
            this.close = close;
            this.fetch = fetch;
        }

        static Dialect of(String product) {
            String p = product.toLowerCase(Locale.ROOT);
            if (p.contains("mysql") || p.contains("mariadb")) return MYSQL;
            if (p.contains("sql server")) return SQLSERVER;
            if (p.contains("oracle") || p.contains("derby")) return ORACLE;
            return STANDARD;
        }
    }

    private static final Pattern TABLE = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?");

    private final DataSource dataSource;
    private final String table;
    private Dialect dialect;
    private Model<T> model;

    public JdbcRepository(DataSource dataSource, String table) {
        if (!TABLE.matcher(table).matches()) throw new IllegalArgumentException("illegal table name '" + table + "'");
        this.dataSource = dataSource;
        this.table = table;
    }

    /** Sets the dialect instead of detecting it from the connection's product name. */
    public JdbcRepository<T> dialect(Dialect dialect) {
        this.dialect = dialect;
        return this;
    }

    @Override
    public void bind(Model<T> model) {
        this.model = model;
    }

    @Override
    public Page<T> page(Query query) {
        try (Connection conn = dataSource.getConnection()) {
            Sql count = new Sql("SELECT COUNT(*) FROM " + table()).where(query.conditions());
            long total;
            try (PreparedStatement ps = count.prepare(conn); ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
            Sql select = new Sql("SELECT * FROM " + table()).where(query.conditions()).add(" ORDER BY ");
            select.add(query.sort().isEmpty() ? q(model.pk()) + " ASC" : query.sort().stream()
                    .map(s -> q(s.getField()) + (s.getDirection() == Direction.DESC ? " DESC" : " ASC")).collect(Collectors.joining(", ")));
            if (dialect().fetch) select.add(" OFFSET ").p(query.offset()).add(" ROWS FETCH NEXT ").p(query.limit()).add(" ROWS ONLY");
            else select.add(" LIMIT ").p(query.limit()).add(" OFFSET ").p(query.offset());
            return new Page<>(total, select.read(conn));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public T get(Object pk) {
        try (Connection conn = dataSource.getConnection()) {
            List<T> rows = new Sql("SELECT * FROM " + table() + " WHERE " + q(model.pk()) + " = ").p(pk).read(conn);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void add(T record) {
        List<String> columns = model.columns().stream().filter(c -> !c.equals(model.pk()) || model.get(record, c) != null).toList();
        Sql sql = new Sql("INSERT INTO " + table() + " (" + columns.stream().map(this::q).collect(Collectors.joining(", ")) + ") VALUES (");
        for (int i = 0; i < columns.size(); i++) sql.add(i == 0 ? "" : ", ").p(model.get(record, columns.get(i)));
        sql.add(")");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = dialect() == Dialect.ORACLE ? conn.prepareStatement(sql.text.toString(), new String[]{model.pk()})
                     : conn.prepareStatement(sql.text.toString(), Statement.RETURN_GENERATED_KEYS)) {
            sql.bind(ps).executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) model.set(record, model.pk(), key(keys));
            }
            if (!conn.getAutoCommit()) conn.commit();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void update(T record) {
        List<String> columns = model.columns().stream().filter(c -> !c.equals(model.pk())).toList();
        if (columns.isEmpty()) return;
        Sql sql = new Sql("UPDATE " + table() + " SET ");
        for (int i = 0; i < columns.size(); i++) sql.add((i == 0 ? "" : ", ") + q(columns.get(i)) + " = ").p(model.get(record, columns.get(i)));
        sql.add(" WHERE " + q(model.pk()) + " = ").p(model.get(record, model.pk())).write();
    }

    @Override
    public void delete(List<Object> pks) {
        if (pks.isEmpty()) return;
        Sql sql = new Sql("DELETE FROM " + table() + " WHERE " + q(model.pk()) + " IN (");
        for (int i = 0; i < pks.size(); i++) sql.add(i == 0 ? "" : ", ").p(pks.get(i));
        sql.add(")").write();
    }

    /** The SQL and parameters {@link #page} would run for {@code conditions}: for tests and logging. */
    String where(List<Condition> conditions, List<Object> params) {
        Sql sql = new Sql("").where(conditions);
        params.addAll(sql.params);
        return sql.text.toString();
    }

    private Object key(ResultSet keys) throws SQLException {
        try {
            return keys.getObject(model.pk());
        } catch (SQLException e) { // a driver that names the generated column differently
            return keys.getObject(1);
        }
    }

    private Dialect dialect() {
        if (dialect == null) {
            try (Connection conn = dataSource.getConnection()) {
                dialect = Dialect.of(conn.getMetaData().getDatabaseProductName());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
        return dialect;
    }

    private String table() {
        return String.join(".", List.of(table.split("\\.")).stream().map(this::q).toList());
    }

    String q(String identifier) {
        Dialect d = dialect();
        return d.open + identifier.replace(d.close, d.close + d.close) + d.close;
    }

    /** SQL text with {@code ?} markers and the values bound to them. */
    private final class Sql {

        final StringBuilder text;
        final List<Object> params = new ArrayList<>();

        Sql(String text) {
            this.text = new StringBuilder(text);
        }

        Sql add(String s) {
            text.append(s);
            return this;
        }

        Sql p(Object value) {
            params.add(value);
            text.append('?');
            return this;
        }

        Sql where(List<Condition> conditions) {
            String glue = " WHERE ";
            for (Condition c : conditions) {
                text.append(glue);
                glue = " AND ";
                String col = q(c.getKey());
                List<?> list = c.getValue() instanceof List<?> l ? l : List.of();
                switch (c.getExpression()) {
                    case EQ -> add(col + " = ").p(c.getValue());
                    case NEQ -> add(col + " <> ").p(c.getValue());
                    case GT -> add(col + " > ").p(c.getValue());
                    case GTE -> add(col + " >= ").p(c.getValue());
                    case LT -> add(col + " < ").p(c.getValue());
                    case LTE -> add(col + " <= ").p(c.getValue());
                    // '!' rather than a backslash: MySQL reads a backslash inside a string literal as an escape itself
                    case LIKE, NOT_LIKE -> add(col + (c.getExpression() == QueryExpression.NOT_LIKE ? " NOT" : "") + " LIKE ")
                            .p("%" + Values.text(c.getValue()).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%").add(" ESCAPE '!'");
                    case RANGE -> add(col + " BETWEEN ").p(list.get(0)).add(" AND ").p(list.get(1));
                    case IN, NOT_IN -> {
                        boolean in = c.getExpression() == QueryExpression.IN;
                        if (list.isEmpty()) {
                            add(in ? "1=0" : "1=1");
                            break;
                        }
                        add(col + (in ? " IN (" : " NOT IN ("));
                        for (int i = 0; i < list.size(); i++) add(i == 0 ? "" : ", ").p(list.get(i));
                        add(")");
                    }
                    case NULL -> add(col + " IS NULL");
                    case NOT_NULL -> add(col + " IS NOT NULL");
                    default -> throw new IllegalArgumentException("illegal operator " + c.getExpression());
                }
            }
            return this;
        }

        PreparedStatement prepare(Connection conn) throws SQLException {
            return bind(conn.prepareStatement(text.toString()));
        }

        PreparedStatement bind(PreparedStatement ps) throws SQLException {
            for (int i = 0; i < params.size(); i++) {
                Object v = Values.toDb(params.get(i));
                if (v == null) ps.setNull(i + 1, Types.NULL);
                else ps.setObject(i + 1, v);
            }
            return ps;
        }

        List<T> read(Connection conn) throws SQLException {
            try (PreparedStatement ps = prepare(conn); ResultSet rs = ps.executeQuery()) {
                // match columns to fields case-insensitively: Oracle and unquoted DDL may report upper case
                Map<String, String> names = new HashMap<>();
                model.columns().forEach(c -> names.put(c.toLowerCase(Locale.ROOT), c));
                ResultSetMetaData meta = rs.getMetaData();
                List<T> rows = new ArrayList<>();
                while (rs.next()) {
                    T record = model.newInstance();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        String column = names.get(meta.getColumnLabel(i).toLowerCase(Locale.ROOT));
                        if (column != null) model.set(record, column, rs.getObject(i));
                    }
                    rows.add(record);
                }
                return rows;
            }
        }

        void write() {
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = prepare(conn)) {
                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
