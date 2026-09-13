package xyz.erupt.designer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2026-09-09
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.designer")
public class EruptDesignerProp {

    // SQLite file holding all designer tables. Relative paths resolve against the working
    // directory; mount it on a persistent volume and include it in backups — it is NOT part
    // of the main database.
    private String dbPath = "data/designer.db";

    // connection pool size; SQLite allows one writer at a time, readers run concurrently in WAL mode
    private Integer maxPoolSize = 4;

}
