package xyz.erupt.upms.spi;

/**
 * A row that belongs to one tenant; {@code null} is the platform.
 *
 * @author YuePeng
 */
public interface TenantScoped {

    String getTenantId();

    void setTenantId(String tenantId);

}
