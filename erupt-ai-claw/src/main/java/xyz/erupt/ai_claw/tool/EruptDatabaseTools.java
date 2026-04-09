//package xyz.erupt.ai_claw.tool;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import dev.langchain4j.agent.tool.P;
//import dev.langchain4j.agent.tool.Tool;
//import jakarta.annotation.Resource;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//import xyz.erupt.ai.annotation.AiToolbox;
//import xyz.erupt.core.config.GsonFactory;
//import xyz.erupt.jpa.dao.EruptDao;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author YuePeng
// */
//@AiToolbox
//@Component
//@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
//public class EruptDatabaseTools {
//
//    private static final int MAX_ROWS = 500;
//
//    private static final String SELECT_ONLY = "Error: only SELECT statements are allowed.";
//
//    @Resource
//    private EruptDao eruptDao;
//
//    @Tool("Execute a native SQL SELECT query against the application database. " +
//            "Call describeTable first if the table structure is unknown. " +
//            "Only SELECT is permitted — INSERT/UPDATE/DELETE/DROP/TRUNCATE are forbidden.")
//    public String executeSql(
//            @P("Native SQL SELECT statement. Non-SELECT statements will be rejected.") String sql) {
//        String trimmed = sql.strip().toLowerCase();
//        if (!trimmed.startsWith("select")) return SELECT_ONLY;
//        try {
//            List<?> rows = eruptDao.getEntityManager()
//                    .createNativeQuery(sql)
//                    .setMaxResults(MAX_ROWS)
//                    .getResultList();
//            return GsonFactory.getGson().toJson(rows);
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }
//
//    @Tool("Inspect database schema. If tableName is blank, lists all table names. " +
//            "If tableName is provided, returns column names and types for that table.")
//    public String describeTable(
//            @P("Table name to describe. Leave blank to list all tables.") String tableName) {
//        try {
//            Connection conn = eruptDao.getEntityManager()
//                    .unwrap(java.sql.Connection.class);
//            var meta = conn.getMetaData();
//            if (tableName == null || tableName.isBlank()) {
//                List<String> tables = new ArrayList<>();
//                try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
//                    while (rs.next()) tables.add(rs.getString("TABLE_NAME"));
//                }
//                return GsonFactory.getGson().toJson(tables);
//            } else {
//                JsonArray columns = new JsonArray();
//                try (ResultSet rs = meta.getColumns(null, null, tableName, "%")) {
//                    while (rs.next()) {
//                        JsonObject col = new JsonObject();
//                        col.addProperty("column", rs.getString("COLUMN_NAME"));
//                        col.addProperty("type", rs.getString("TYPE_NAME"));
//                        col.addProperty("nullable", "YES".equals(rs.getString("IS_NULLABLE")));
//                        columns.add(col);
//                    }
//                }
//                if (columns.isEmpty()) return "Table not found or has no columns: " + tableName;
//                return GsonFactory.getGson().toJson(columns);
//            }
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }
//
//}
