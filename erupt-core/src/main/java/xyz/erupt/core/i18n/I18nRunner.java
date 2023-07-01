package xyz.erupt.core.i18n;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

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
public class I18nRunner extends LinkedCaseInsensitiveMap<Map<String, String>> implements ApplicationRunner {

    //语言文件对应文字映射
    private static final I18nRunner langMappings = new I18nRunner();

    private static final String I18N_EXT = ".csv";

    public static String getI18nValue(String lang, String key) {
        if (null == langMappings.get(lang)) {
            return key;
        }
        return Optional.ofNullable(langMappings.get(lang).get(key)).orElse(key);
    }

    @Override
    @SneakyThrows
    public void run(ApplicationArguments args) {
        Enumeration<URL> urls = I18nRunner.class.getClassLoader().getResources("i18n/");
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
    }

    @SneakyThrows
    private void scanFile(File file) {
        BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        if (fileAttributes.isRegularFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(I18N_EXT)) {
                @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
                @Cleanup InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                this.writeLang(inputStreamReader);
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
                    this.writeLang(inputStreamReader);
                }
            }
        }
    }

    @SneakyThrows
    private void writeLang(InputStreamReader inputStreamReader) {
        @Cleanup BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        List<String> header = new ArrayList<>();
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            for (int i = 0; i < row.length; i++) {
                if (first) {
                    header.add(row[i]);
                    if (!langMappings.containsKey(row[i])) {
                        langMappings.put(row[i], new HashMap<>());
                    }
                } else {
                    if (i < header.size()) {
                        if (null != row[i]) {
                            if (row[i].startsWith("\"") && row[i].endsWith("\"")) {
                                row[i] = row[i].substring(1, row[i].length() - 1);
                            }
                            langMappings.get(header.get(i)).put(row[0], row[i]);
                        }
                    }
                }
            }
            first = false;
        }
    }

}
