package xyz.erupt.core.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt")
@SuppressWarnings("ConfigurationProperties")
public class EruptProp {

    // Hot-build erupt: when enabled, erupt will be rebuilt on every request. Useful for modifying erupt annotations at startup. Do NOT enable in production.
    private boolean hotBuild = false;

    // Check on add, edit, cell edit and import that CHOICE / MULTI_CHOICE values are among the options the user is offered;
    // a field that legitimately stores other values opts out with validate = false
    private boolean choiceValidate = true;

    // Root path for attachment storage
    private String uploadPath = "/opt/erupt-attachment";

    // Whether to enable CSRF validation
    private boolean csrfInspect = true;

    // Whether to preserve the original filename of uploaded files
    private boolean keepUploadFileName = false;

    // Seed module menus on start (erupt.init-modules-menu); a module's initFun runs before its first
    // seeding, so it goes with it. Turn off for a read-only database user, menus managed by scripts,
    // or all but one of several applications sharing a database
    private boolean initModulesMenu = true;

    // Specify which packages use Gson for serialization
    private String[] gsonHttpMessageConvertersPackages;

    // Whether to use Redis for session management
    private boolean redisSession = false;

    // Whether to automatically refresh the token expiry in the Redis session (effective when redisSession is true)
    private boolean redisSessionRefresh = false;

    // Default locale
    public String defaultLocales = "en-US";

    // Log collection feature switch
    private boolean logTrack = true;

    // Number of rows to buffer for log collection
    private Integer logTrackCacheSize = 1000;

}
