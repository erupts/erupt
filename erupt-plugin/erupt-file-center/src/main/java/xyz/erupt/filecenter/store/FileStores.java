package xyz.erupt.filecenter.store;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.filecenter.prop.EruptFileCenterProperties;

import java.nio.file.Path;
import java.util.*;

/**
 * The stores the page offers, in order: the upload directory, the extra local folders of
 * {@code erupt.file-center.roots}, the S3 bucket when erupt-data-s3 is present and configured,
 * then every entry of {@code erupt.file-center.stores} (local, S3 or SFTP).
 * Any other {@link FileStore} bean joins the list too.
 *
 * @author YuePeng
 */
@Component
public class FileStores {

    public static final String UPLOADS = "Uploads";

    private final Map<String, FileStore> stores = new LinkedHashMap<>();

    private final EruptProp eruptProp;

    private final EruptFileCenterProperties properties;

    private final ObjectProvider<FileStore> custom;

    private final ApplicationContext context;

    public FileStores(EruptProp eruptProp, EruptFileCenterProperties properties, ObjectProvider<FileStore> custom, ApplicationContext context) {
        this.eruptProp = eruptProp;
        this.properties = properties;
        this.custom = custom;
        this.context = context;
    }

    @PostConstruct
    public void init() {
        this.add(new LocalFileStore(UPLOADS, Path.of(eruptProp.getUploadPath()), EruptRestPath.ERUPT_ATTACHMENT));
        properties.getRoots().forEach((name, dir) -> this.add(new LocalFileStore(name, Path.of(dir), null)));
        this.attachmentBucket().ifPresent(this::add);
        for (EruptFileCenterProperties.Store store : properties.getStores()) this.add(this.configured(store));
        custom.orderedStream().forEach(this::add);
    }

    private static boolean s3Present() {
        return ClassUtils.isPresent("software.amazon.awssdk.services.s3.S3Client", FileStores.class.getClassLoader());
    }

    // The bucket attachments go to, when erupt-data-s3 is present: its classes are only touched then
    private Optional<FileStore> attachmentBucket() {
        if (!s3Present() || !ClassUtils.isPresent("xyz.erupt.s3.prop.EruptS3Properties", getClass().getClassLoader())) return Optional.empty();
        xyz.erupt.s3.prop.EruptS3Properties prop = context.getBeanProvider(xyz.erupt.s3.prop.EruptS3Properties.class).getIfAvailable();
        if (null == prop || StringUtils.isBlank(prop.getBucket())) return Optional.empty();
        return Optional.of(new S3FileStore("S3 · " + prop.getBucket(), new S3Settings(prop.getBucket(), prop.getRegion(), prop.getEndpoint(),
                prop.getAccessKey(), prop.getSecretKey(), prop.isPathStyle(), prop.getPrefix(), prop.getDomain())));
    }

    private FileStore configured(EruptFileCenterProperties.Store store) {
        if (StringUtils.isBlank(store.getName())) throw new EruptWebApiRuntimeException("erupt.file-center.stores: every store needs a name");
        if (EruptFileCenterProperties.Store.S3.equalsIgnoreCase(store.getType())) {
            if (!s3Present()) throw new EruptWebApiRuntimeException("erupt.file-center.stores." + store.getName() + ": an S3 store needs the AWS S3 SDK (erupt-data-s3) on the classpath");
            if (StringUtils.isBlank(store.getBucket())) throw new EruptWebApiRuntimeException("erupt.file-center.stores." + store.getName() + ": bucket is required");
            return new S3FileStore(store.getName(), new S3Settings(store.getBucket(), store.getRegion(), store.getEndpoint(),
                    store.getAccessKey(), store.getSecretKey(), store.isPathStyle(), store.getPrefix(), store.getDomain()));
        }
        if (EruptFileCenterProperties.Store.SFTP.equalsIgnoreCase(store.getType())) {
            if (!ClassUtils.isPresent("com.jcraft.jsch.JSch", FileStores.class.getClassLoader())) throw new EruptWebApiRuntimeException("erupt.file-center.stores." + store.getName() + ": an SFTP store needs JSch (com.github.mwiede:jsch) on the classpath");
            if (StringUtils.isBlank(store.getHost()) || StringUtils.isBlank(store.getUsername())) throw new EruptWebApiRuntimeException("erupt.file-center.stores." + store.getName() + ": host and username are required");
            return new SftpFileStore(store.getName(), new SftpSettings(store.getHost(), store.getPort(), store.getUsername(), store.getPassword(),
                    store.getPrivateKey(), store.getPassphrase(), store.getKnownHosts(), store.getPath()));
        }
        if (StringUtils.isBlank(store.getPath())) throw new EruptWebApiRuntimeException("erupt.file-center.stores." + store.getName() + ": path is required");
        return new LocalFileStore(store.getName(), Path.of(store.getPath()), null);
    }

    public void add(FileStore store) {
        stores.put(store.name(), store);
    }

    public List<String> names() {
        return new ArrayList<>(stores.keySet());
    }

    public FileStore get(String name) {
        FileStore store = stores.get(name);
        if (null == store) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.store_unknown") + " → " + name);
        return store;
    }

}
