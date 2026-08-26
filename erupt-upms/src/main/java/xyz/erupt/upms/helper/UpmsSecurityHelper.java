package xyz.erupt.upms.helper;

import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.upms.constant.EncryptType;

/**
 * Shared password/security logic for every UPMS user entity (platform side and tenant side).
 * Keeps the salt / encryptType / password triple and the verification algorithm in one place,
 * so a security fix applies to all login paths at once.
 *
 * @author YuePeng
 */
public class UpmsSecurityHelper {

    /**
     * Contract implemented by any user entity that stores an encryptable password.
     * Both {@code EruptUser} and the tenant-side user entity expose these setters (via Lombok).
     */
    public interface PasswordHolder {

        void setPassword(String password);

        void setSalt(String salt);

        void setEncryptType(String encryptType);

    }

    /**
     * Apply a raw password onto the holder, encrypting it with a fresh salt when required.
     * When encryption is off the credential is stored as-is and salt / encryptType are cleared.
     */
    public static void applyPassword(PasswordHolder holder, String rawPwd, boolean encrypt) {
        if (encrypt) {
            String salt = EncryptUtil.generateSalt();
            holder.setSalt(salt);
            holder.setEncryptType(EncryptType.SHA512);
            holder.setPassword(EncryptUtil.digestSHA512Salt(rawPwd, salt));
        } else {
            holder.setSalt(null);
            holder.setEncryptType(null);
            holder.setPassword(rawPwd);
        }
    }

    /**
     * Verify an input password against stored credentials.
     */
    public static boolean checkPwd(String storedPassword, Boolean encrypt, String salt, String encryptType, String inputPwd) {
        String checkPwd;
        if (EncryptType.SHA512.equalsIgnoreCase(encryptType)) {
            checkPwd = EncryptUtil.digestSHA512Salt(inputPwd, salt);
        } else {
            checkPwd = (null != encrypt && encrypt) ? EncryptUtil.digest(inputPwd) : inputPwd;
        }
        return checkPwd.equals(storedPassword);
    }

}
