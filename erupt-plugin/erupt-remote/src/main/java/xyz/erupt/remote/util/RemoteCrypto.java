package xyz.erupt.remote.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.remote.config.EruptRemoteProp;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM encryption for credentials stored at rest.
 * The key is derived from {@code erupt.remote.secret-key} when configured; otherwise a random key is generated once
 * and kept in {@code .erupt/remote.key}. Ciphertext is prefixed so plain values can be told apart.
 *
 * @author YuePeng
 */
@Slf4j
@Component
public class RemoteCrypto {

    private static final String PREFIX = "enc:";

    private static final int IV_LENGTH = 12;

    private static final int TAG_BITS = 128;

    private static final String KEY_FILE = "remote.key";

    private final SecureRandom random = new SecureRandom();

    @Resource
    private EruptRemoteProp prop;

    private SecretKeySpec key;

    @PostConstruct
    public void init() throws Exception {
        String secret = StringUtils.isNotBlank(prop.getSecretKey()) ? prop.getSecretKey().trim() : loadOrCreateLocalKey();
        if (secret == null) return;
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(digest, "AES");
    }

    /**
     * Default key for single-node setups: generated once and reused from {@code .erupt/remote.key}.
     * Multi-node deployments must share the same key via {@code erupt.remote.secret-key}.
     */
    private String loadOrCreateLocalKey() {
        Path file = Paths.get(EruptConst.ERUPT_DIR_PATH, KEY_FILE);
        try {
            if (Files.exists(file)) {
                String existing = Files.readString(file, StandardCharsets.UTF_8).trim();
                if (!existing.isEmpty()) return existing;
            }
            byte[] raw = new byte[32];
            random.nextBytes(raw);
            String generated = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
            Files.createDirectories(file.getParent());
            Files.writeString(file, generated, StandardCharsets.UTF_8);
            try {
                Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException ignored) {
                // Non-POSIX file system (Windows): rely on the directory ACL
            }
            log.info("[erupt-remote] Generated credential key at {}; set erupt.remote.secret-key to share one key across nodes", file.toAbsolutePath());
            return generated;
        } catch (IOException e) {
            log.warn("[erupt-remote] Cannot read or create {}: {}; host passwords cannot be stored until erupt.remote.secret-key is set", file.toAbsolutePath(), e.getMessage());
            return null;
        }
    }

    public boolean isConfigured() {
        return key != null;
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public String encrypt(String plain) {
        if (!isConfigured()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.secret_key_missing"));
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException("Credential encryption failed: " + e.getMessage());
        }
    }

    /**
     * @return the plain text, or {@code null} when the value is empty or cannot be decrypted with the current key
     */
    public String decrypt(String stored) {
        if (StringUtils.isBlank(stored)) return null;
        if (!stored.startsWith(PREFIX)) return null;
        if (!isConfigured()) return null;
        try {
            byte[] all = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, all, 0, IV_LENGTH));
            return new String(cipher.doFinal(all, IV_LENGTH, all.length - IV_LENGTH), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[erupt-remote] Stored credential cannot be decrypted, was erupt.remote.secret-key changed?");
            return null;
        }
    }
}
