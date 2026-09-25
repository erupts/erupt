package xyz.erupt.generator.handler;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.service.CodeRender;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Hands the generated classes over as files: one is a java file, several are an archive.
 * The bytes ride the response of a request that was already authorised, so no download
 * endpoint has to be opened for them.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Component
public class CodeDownloadHandler implements OperationHandler<GeneratorClass, Void> {

    private static final String ZIP_NAME = "erupt-code.zip";

    private static final String JAVA_MIME = "text/x-java-source";

    private static final String ZIP_MIME = "application/zip";

    @Override
    public String exec(List<GeneratorClass> data, Void unused, String[] param) {
        if (1 == data.size()) {
            GeneratorClass clazz = data.get(0);
            return download(CodeFiles.name(clazz), CodeRender.render(clazz).getBytes(StandardCharsets.UTF_8), JAVA_MIME);
        }
        return download(ZIP_NAME, zip(data), ZIP_MIME);
    }

    private static String download(String name, byte[] bytes, String mime) {
        return "downloadFile(" + GsonFactory.getGson().toJson(name) + ", "
                + GsonFactory.getGson().toJson(Base64.getEncoder().encodeToString(bytes)) + ", "
                + GsonFactory.getGson().toJson(mime) + ")";
    }

    @SneakyThrows
    private static byte[] zip(List<GeneratorClass> classes) {
        Set<String> entries = new HashSet<>();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (GeneratorClass clazz : classes) {
                String entry = CodeFiles.path(clazz);
                //two tables may well answer to the same class name, neither of them may be lost
                for (int i = 2; !entries.add(entry); i++) {
                    entry = CodeFiles.path(clazz).replace(".java", "_" + i + ".java");
                }
                zip.putNextEntry(new ZipEntry(entry));
                zip.write(CodeRender.render(clazz).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

}
