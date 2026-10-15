package xyz.erupt.cloud.node.lite;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The SQL the repository writes, and the shared scenario against H2 (DatabaseTest runs it on PostgreSQL and MySQL). */
class JdbcRepositoryTest {

    @Erupt(name = "DbPost")
    static class DbPost {
        @EruptField
        Long id;
        @EruptField(edit = @Edit(title = "Title", search = @Search))
        String title;
        @EruptField(edit = @Edit(title = "Views"))
        Integer views;
        @EruptField(edit = @Edit(title = "Live"))
        Boolean live;
        @EruptField(edit = @Edit(title = "Day"))
        LocalDate day;
        @EruptField(edit = @Edit(title = "At"))
        LocalDateTime at;

        static DbPost of(String title, Integer views, Boolean live, LocalDate day, LocalDateTime at) {
            DbPost p = new DbPost();
            p.title = title;
            p.views = views;
            p.live = live;
            p.day = day;
            p.at = at;
            return p;
        }
    }

    static final String H2_DDL = "CREATE TABLE \"db_post\" (\"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"title\" VARCHAR(255), \"views\" INT, "
                                 + "\"live\" BOOLEAN, \"day\" DATE, \"at\" TIMESTAMP(3))";

    static DataSource h2() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        return ds;
    }

    static void execute(DataSource ds, String... sql) throws Exception {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            for (String s : sql) st.execute(s);
        }
    }

    static Condition c(String field, QueryExpression op, Object value) {
        return new Condition(field, value, op);
    }

    @Test
    void sql() {
        JdbcRepository<DbPost> repo = new JdbcRepository<DbPost>(h2(), "db_post").dialect(JdbcRepository.Dialect.MYSQL);
        repo.bind(Model.of(DbPost.class));
        List<Object> params = new ArrayList<>();
        String where = repo.where(List.of(c("title", QueryExpression.LIKE, "50%_off!"), c("views", QueryExpression.IN, List.of()),
                c("views", QueryExpression.NOT_IN, List.of(1, 2)), c("day", QueryExpression.RANGE, List.of(1, 2)), c("live", QueryExpression.NULL, null)), params);
        assertEquals(" WHERE `title` LIKE ? ESCAPE '!' AND 1=0 AND `views` NOT IN (?, ?) AND `day` BETWEEN ? AND ? AND `live` IS NULL", where);
        assertEquals(List.of("%50!%!_off!!%", 1, 2, 1, 2), params);
        assertEquals("`a``b`", repo.q("a`b"));
        assertEquals("[a]]b]", new JdbcRepository<DbPost>(h2(), "t").dialect(JdbcRepository.Dialect.SQLSERVER).q("a]b"));
        assertThrows(IllegalArgumentException.class, () -> new JdbcRepository<DbPost>(h2(), "t; DROP TABLE x"));
    }

    @Test
    void h2Scenario() throws Exception {
        DataSource ds = h2();
        execute(ds, H2_DDL);
        scenario(ds);
    }

    /** The behaviour every database must show; the table db_post exists and is empty. */
    static void scenario(DataSource ds) {
        JdbcRepository<DbPost> repo = new JdbcRepository<>(ds, "db_post");
        repo.bind(Model.of(DbPost.class));
        DbPost first = DbPost.of("100% pure", 3, true, LocalDate.of(2026, 1, 5), LocalDateTime.of(2026, 1, 5, 10, 30, 0, 123_000_000));
        repo.add(first);
        assertEquals(1L, first.id, "generated key set on the record");
        repo.add(DbPost.of("wow!", 1, false, LocalDate.of(2026, 3, 1), null));
        repo.add(DbPost.of("a_b", null, false, null, null));
        assertEquals(List.of("100% pure", "wow!", "a_b"), titles(repo, List.of(), List.of()), "default order is primary key ascending");
        List<Sort> byTitle = List.of(new Sort("title", Direction.ASC));
        assertEquals(List.of("100% pure"), titles(repo, List.of(c("title", QueryExpression.LIKE, "%")), byTitle), "LIKE escapes %");
        assertEquals(List.of("wow!"), titles(repo, List.of(c("title", QueryExpression.LIKE, "!")), byTitle), "LIKE escapes !");
        assertEquals(List.of("a_b"), titles(repo, List.of(c("title", QueryExpression.LIKE, "_")), byTitle), "LIKE escapes _");
        assertEquals(List.of("a_b", "wow!"), titles(repo, List.of(c("title", QueryExpression.NOT_LIKE, "pure")), byTitle), "NOT_LIKE");
        assertEquals(List.of("100% pure", "wow!"), titles(repo, List.of(c("views", QueryExpression.IN, List.of(1, 3))), byTitle), "IN");
        assertEquals(List.of(), titles(repo, List.of(c("views", QueryExpression.IN, List.of())), byTitle), "empty IN");
        assertEquals(List.of("wow!"), titles(repo, List.of(c("day", QueryExpression.RANGE, List.of(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 31)))), byTitle), "RANGE");
        assertEquals(List.of("100% pure"), titles(repo, List.of(c("live", QueryExpression.EQ, true)), byTitle), "boolean");
        assertEquals(List.of("100% pure"), titles(repo, List.of(c("views", QueryExpression.GT, 2)), byTitle), "GT");
        assertEquals(List.of("a_b"), titles(repo, List.of(c("views", QueryExpression.NULL, null)), byTitle), "NULL");
        Repository.Page<DbPost> page = repo.page(new Query(List.of(), List.of(new Sort("views", Direction.DESC)), 1, 1));
        assertEquals(3, page.total());
        assertEquals(1, page.records().size());

        DbPost row = repo.get(1L);
        assertEquals("100% pure", row.title);
        assertInstanceOf(Integer.class, row.views);
        assertEquals(LocalDate.of(2026, 1, 5), row.day);
        assertEquals(LocalDateTime.of(2026, 1, 5, 10, 30, 0, 123_000_000), row.at, "round trip");
        row.title = "renamed";
        row.live = false;
        repo.update(row);
        assertEquals("renamed", repo.get(1L).title);
        assertEquals(false, repo.get(1L).live);
        repo.delete(List.of(1L, 2L));
        assertEquals(List.of("a_b"), titles(repo, List.of(), List.of()));
        assertNull(repo.get(1L));
    }

    static List<String> titles(JdbcRepository<DbPost> repo, List<Condition> conditions, List<Sort> sort) {
        return repo.page(new Query(conditions, sort, 0, 10)).records().stream().map(p -> p.title).toList();
    }
}
