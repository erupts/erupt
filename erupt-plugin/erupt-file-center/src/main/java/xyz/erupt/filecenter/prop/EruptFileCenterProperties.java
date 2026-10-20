package xyz.erupt.filecenter.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * erupt:
 *   file-center:
 *     roots:                         # extra local folders, by the name the page shows
 *       Reports: /data/reports
 *     stores:                        # any number of stores: local, s3 or sftp, each with its own settings
 *       - name: Backups
 *         type: local
 *         path: /var/backups
 *       - name: Partner SFTP
 *         type: sftp
 *         host: sftp.example.com
 *         port: 22
 *         username: exchange
 *         password: ${SFTP_PASSWORD}  # or private-key: a PEM text or key file path, with passphrase
 *         path: /outbound            # the remote directory that is the store's root
 *         known-hosts: /etc/ssh/ssh_known_hosts   # optional; without it any host key is accepted
 *       - name: Media
 *         type: s3
 *         bucket: media
 *         endpoint: https://oss-cn-hangzhou.aliyuncs.com
 *         region: cn-hangzhou
 *         access-key: ${MEDIA_AK}
 *         secret-key: ${MEDIA_SK}
 *         path-style: false
 *         prefix: public            # the store's root inside the bucket
 *         domain: https://cdn.example.com
 *     text-preview-bytes: 262144     # the most of a text file the preview reads
 * </pre>
 * The upload directory ({@code erupt.upload-path}) is always the first store; the bucket of
 * {@code erupt.s3} follows when erupt-data-s3 is present.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.file-center")
public class EruptFileCenterProperties {

    private Map<String, String> roots = new LinkedHashMap<>();

    private List<Store> stores = new ArrayList<>();

    private int textPreviewBytes = 256 * 1024;

    @Getter
    @Setter
    public static class Store {

        public static final String LOCAL = "local";

        public static final String S3 = "s3";

        public static final String SFTP = "sftp";

        private String name;

        private String type = LOCAL;

        // local: the folder; sftp: the remote directory that is the root
        private String path;

        // sftp
        private String host;

        private int port = 22;

        private String username;

        private String password;

        private String privateKey;

        private String passphrase;

        private String knownHosts;

        // s3
        private String bucket;

        private String region;

        private String endpoint;

        private String accessKey;

        private String secretKey;

        private boolean pathStyle;

        private String prefix;

        private String domain;

    }

}
