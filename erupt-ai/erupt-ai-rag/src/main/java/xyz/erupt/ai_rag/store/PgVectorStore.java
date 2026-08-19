package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.prop.VectorStoreProp;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * PostgreSQL pgvector. URI format: "postgresql://user:password@host:5432/db"
 * (password may be omitted and supplied via api-key); a blank URI reuses the
 * application's own datasource, so an app already running on PostgreSQL needs
 * zero extra infrastructure. Each collection maps to a table.
 *
 * @author YuePeng
 * date 2026/8/19
 */
@Component
@ConditionalOnClass(PgVectorEmbeddingStore.class)
public class PgVectorStore extends VectorStoreCore {

    private static final int DEFAULT_PORT = 5432;

    private final ObjectProvider<DataSource> dataSource;

    public PgVectorStore(ObjectProvider<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public VectorStoreType code() {
        return VectorStoreType.PGVECTOR;
    }

    @Override
    public EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension) {
        validateIdentifier(collection);
        if (StringUtils.isBlank(config.getUri())) {
            return PgVectorEmbeddingStore.datasourceBuilder()
                    .datasource(this.applicationDataSource())
                    .table(collection).dimension(dimension).createTable(true)
                    .build();
        }
        PgUri uri = PgUri.parse(config);
        return PgVectorEmbeddingStore.builder()
                .host(uri.host()).port(uri.port()).database(uri.database())
                .user(uri.user()).password(uri.password())
                .table(collection).dimension(dimension).createTable(true)
                .build();
    }

    @Override
    @SneakyThrows
    public void dropCollection(VectorStoreProp config, String collection) {
        validateIdentifier(collection);
        try (Connection connection = this.openConnection(config); Statement statement = connection.createStatement()) {
            // Table identifiers cannot be parameterized; validateIdentifier above
            // guards the internally generated name (erupt_kb_{id})
            statement.executeUpdate("DROP TABLE IF EXISTS " + collection);
        }
    }

    @Override
    @SneakyThrows
    public void healthCheck(VectorStoreProp config) {
        try (Connection connection = this.openConnection(config)) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (!"PostgreSQL".equalsIgnoreCase(product)) {
                throw new EruptWebApiRuntimeException("PGVECTOR requires PostgreSQL, but the datasource is: " + product);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("select 1 from pg_available_extensions where name = 'vector'")) {
                if (!rs.next()) {
                    throw new EruptWebApiRuntimeException("The vector extension is not available on this PostgreSQL server, install pgvector first");
                }
            }
        }
    }

    @SneakyThrows
    private Connection openConnection(VectorStoreProp config) {
        if (StringUtils.isBlank(config.getUri())) {
            return this.applicationDataSource().getConnection();
        }
        PgUri uri = PgUri.parse(config);
        return DriverManager.getConnection(uri.jdbcUrl(), uri.user(), uri.password());
    }

    private DataSource applicationDataSource() {
        DataSource ds = dataSource.getIfAvailable();
        if (null == ds) {
            throw new EruptWebApiRuntimeException("PGVECTOR with a blank uri reuses the application datasource, but none is available");
        }
        return ds;
    }

    private static void validateIdentifier(String collection) {
        if (null == collection || !collection.matches("[A-Za-z0-9_]+")) {
            throw new EruptWebApiRuntimeException("Illegal collection identifier: " + collection);
        }
    }

    private record PgUri(String host, int port, String database, String user, String password) {

        // "postgresql://user:password@host:5432/db"; password falls back to api-key
        static PgUri parse(VectorStoreProp config) {
            URI uri = URI.create(config.getUri());
            String user = null, password = null;
            if (null != uri.getUserInfo()) {
                String[] parts = uri.getUserInfo().split(":", 2);
                user = parts[0];
                password = parts.length > 1 ? parts[1] : null;
            }
            return new PgUri(
                    uri.getHost(),
                    uri.getPort() > 0 ? uri.getPort() : DEFAULT_PORT,
                    StringUtils.removeStart(uri.getPath(), "/"),
                    user,
                    StringUtils.defaultIfBlank(password, config.getApiKey())
            );
        }

        String jdbcUrl() {
            return "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }

    }

}
