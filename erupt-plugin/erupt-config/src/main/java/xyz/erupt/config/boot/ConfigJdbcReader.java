package xyz.erupt.config.boot;

import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Reads the layered profiles' items with plain JDBC, for the moment before the context exists.
 * Identifiers are quoted when Hibernate quotes them, so the names match what schema generation
 * created on a database that folds unquoted names (Oracle upper-cases them).
 *
 * @author YuePeng
 */
public final class ConfigJdbcReader {

    private ConfigJdbcReader() {
    }

    /**
     * @param profiles the layers in override order, see {@link ConfigProfile#layers}
     * @param quoted   whether Hibernate created the tables with quoted identifiers
     * @return the merged key/value map, a later profile overriding an earlier one
     */
    public static Map<String, Object> read(String url, String username, String password, List<String> profiles, boolean quoted) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String q = quoted ? connection.getMetaData().getIdentifierQuoteString().trim() : "";
            UnaryOperator<String> id = name -> q + name + q;
            String sql = "select p." + id.apply(ConfigProfile.CODE_COLUMN) + ", i." + id.apply(ConfigItem.KEY_COLUMN) + ", i." + id.apply(ConfigItem.TYPE_COLUMN)
                    + ", i." + id.apply(ConfigItem.VALUE_COLUMN) + ", i." + id.apply(ConfigItem.ENABLED_COLUMN) + " from " + id.apply(ConfigItem.TABLE) + " i join " + id.apply(ConfigProfile.TABLE)
                    + " p on i." + id.apply(ConfigItem.PROFILE_COLUMN) + " = p." + id.apply("id")
                    + " where p." + id.apply(ConfigProfile.CODE_COLUMN) + " in (" + String.join(",", Collections.nCopies(profiles.size(), "?")) + ")";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < profiles.size(); i++) statement.setString(i + 1, profiles.get(i));
                List<List<String[]>> layers = new ArrayList<>();
                for (int i = 0; i < profiles.size(); i++) layers.add(new ArrayList<>());
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        // compared here, not in SQL: how a boolean column is spelled differs per database
                        if (!rows.getBoolean(5)) continue;
                        layers.get(profiles.indexOf(rows.getString(1))).add(new String[]{rows.getString(2), rows.getString(3), rows.getString(4)});
                    }
                }
                Map<String, Object> values = new LinkedHashMap<>();
                for (List<String[]> layer : layers) for (String[] row : layer) ConfigValues.flatten(row[0], row[1], row[2], values);
                return values;
            }
        }
    }

}
