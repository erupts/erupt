package xyz.erupt.core.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import xyz.erupt.core.constant.EruptConst;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2019-05-21.
 */
@Slf4j
public class ProjectUtil {

    private static final String LOADED_EXT = ".loaded";

    // one-shot per startup: modules skip one by one, but a single summary line is enough
    private static final AtomicBoolean SKIP_HINTED = new AtomicBoolean();

    /**
     * @param projectName identification name
     * @param first       bool flag, indicating whether the function is being called for the first time
     */
    public void projectStartLoaded(String projectName, Consumer<Boolean> first) {
        this.projectStartLoaded(projectName, first, Runnable::run);
    }

    /**
     * @param projectName identification name
     * @param first       bool flag, indicating whether the function is being called for the first time
     * @param markLoaded  decides when the loaded marker file is written; callers whose first-time
     *                    work is transactional should defer it until the transaction commits,
     *                    otherwise a rollback leaves a marker file without the data it stands for
     */
    public void projectStartLoaded(String projectName, Consumer<Boolean> first, Consumer<Runnable> markLoaded) {
        File dirFile = new File(EruptConst.ERUPT_DIR_PATH);
        String warnTxt = " The erupt initialization ID file could not be created";
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            log.warn("{} {}", dirFile, warnTxt);
        }
        File file = new File(dirFile.getPath(), projectName + LOADED_EXT);
        if (file.exists()) {
            if (SKIP_HINTED.compareAndSet(false, true)) {
                log.info("Initialized modules are skipped. Delete the '{}' files in {} to re-initialize", LOADED_EXT, dirFile.getAbsolutePath());
            }
            log.debug("'{}' already initialized, skip", projectName);
            first.accept(false);
        } else {
            first.accept(true);
            markLoaded.accept(() -> this.createLoadedFile(file, warnTxt));
        }
    }

    @SneakyThrows
    private void createLoadedFile(File file, String warnTxt) {
        if (file.createNewFile()) {
            FileUtils.writeStringToFile(file, EruptInformation.getEruptVersion(), StandardCharsets.UTF_8.name());
        } else {
            log.warn("{} {}", file, warnTxt);
        }
    }

}
