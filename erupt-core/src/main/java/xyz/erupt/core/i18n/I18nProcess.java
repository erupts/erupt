package xyz.erupt.core.i18n;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.I18NTranslateService;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author YuePeng
 * date 2021/9/15 00:46
 */
@Service
@Slf4j
public class I18nProcess extends HashMap<String, Map<String, String>> implements ApplicationRunner {

    //语言文件对应文字映射
    private static final I18nProcess langMappings = new I18nProcess();
    private static final String I18N_EXT = "i18n.csv";
    @Resource
    private EruptProp eruptProp;
    @Resource
    private I18NTranslateService i18NTranslateService;

    public static Map<String, String> getLangMapping(String lang) {
        return langMappings.get(lang);
    }

    @Override
    @SneakyThrows
    public void run(ApplicationArguments args) {
        if (eruptProp.isI18n()) {
            Enumeration<URL> urls = I18nProcess.class.getClassLoader().getResources("i18n/");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                switch (url.getProtocol()) {
                    case "file":
                        scanFile(new File(URLDecoder.decode(url.getFile(), Charset.defaultCharset().name())));
                        break;
                    case "jar":
                        JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                        scanJar(urlConnection.getJarFile());
                        break;
                }
            }
//            i18NTranslateService.registerI18NMapping(langMappings);
        }
    }

    @SneakyThrows
    private void scanFile(File file) {
        BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        if (fileAttributes.isRegularFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(I18N_EXT)) {
                @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
                @Cleanup InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                this.putLang(inputStreamReader);
            }
        } else if (fileAttributes.isDirectory()) {
            Optional.ofNullable(file.listFiles()).ifPresent(files -> Arrays.stream(files).forEach(this::scanFile));
        }
    }

    @SneakyThrows
    private void scanJar(JarFile jar) {
        Enumeration<JarEntry> jarEntryEnumeration = jar.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            final JarEntry entry = jarEntryEnumeration.nextElement();
            if (entry.getName().endsWith(I18N_EXT)) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(
                        jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                    this.putLang(inputStreamReader);
                }
            }
        }
    }

    @SneakyThrows
    private void putLang(InputStreamReader inputStreamReader) {
        @Cleanup BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        List<String> header = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            for (int i = 0; i < row.length; i++) {
                if (header.size() == 0) {
                    header.add(row[i]);
                    if (!langMappings.containsKey(row[i])) {
                        langMappings.put(row[i], new HashMap<>());
                    }
                } else {
                    if (row.length == header.size()) {
                        langMappings.get(header.get(i)).put(row[0], row[i]);
                    }
                }
            }
        }
    }

}
