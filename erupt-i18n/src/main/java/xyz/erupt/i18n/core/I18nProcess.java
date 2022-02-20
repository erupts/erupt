package xyz.erupt.i18n.core;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import xyz.erupt.core.service.I18NTranslateService;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author YuePeng
 * date 2021/9/15 00:46
 */
@Service
@Slf4j
public class I18nProcess extends HashMap<String, Properties> implements ApplicationRunner {

    //语言文件对应文字映射
    private static final I18nProcess langMappings = new I18nProcess();
    private static final String I18N_EXT = "properties";

    @Resource
    private I18NTranslateService i18NTranslateService;

    public static Properties getLangMapping(String lang) {
        return langMappings.get(lang);
    }

    @Override
    @SneakyThrows
    public void run(ApplicationArguments args) {
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
        i18NTranslateService.registerI18NMapping(langMappings);
    }

    @SneakyThrows
    private void scanFile(File file) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(I18N_EXT)) {
                String lang = this.getFileLang(fileName);
                Properties properties = new Properties();
                @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
                @Cleanup InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                properties.load(inputStreamReader);
                if (langMappings.containsKey(lang)) {
                    langMappings.get(lang).putAll(properties);
                } else {
                    langMappings.put(lang, properties);
                }
            }
        } else if (file.isDirectory()) {
            Optional.ofNullable(file.listFiles()).ifPresent(files -> Arrays.stream(files).forEach(this::scanFile));
        }
    }

    @SneakyThrows
    private void scanJar(JarFile jar) {
        Enumeration<JarEntry> jarEntryEnumeration = jar.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            final JarEntry entry = jarEntryEnumeration.nextElement();
            if (entry.getName().endsWith(I18N_EXT)) {
                String lang = this.getFileLang(entry.getName());
                Properties properties = new Properties();
                try (InputStreamReader inputStreamReader = new InputStreamReader(
                        jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                    properties.load(inputStreamReader);
                    if (langMappings.containsKey(lang)) {
                        langMappings.get(lang).putAll(properties);
                    } else {
                        langMappings.put(lang, properties);
                    }
                }
            }
        }
    }

    private String getFileLang(String fileName) {
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        String[] split = fileName.substring(0, fileName.indexOf(I18N_EXT) - 1).split("_");
        return split[split.length - 1].toLowerCase();
    }

}
