package xyz.erupt.filecenter.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.R;
import xyz.erupt.filecenter.store.FileEntry;
import xyz.erupt.filecenter.store.FileStore;
import xyz.erupt.filecenter.store.FileStores;
import xyz.erupt.upms.service.EruptUserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * What the page calls. Every call needs the page's menu; a change needs the write button too.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(FileCenterController.PATH)
public class FileCenterController implements EruptRouter.VerifyHandler {

    public static final String PATH = EruptRestPath.ERUPT_API + "/file-center";

    private final FileStores stores;

    private final EruptUserService userService;

    public FileCenterController(FileStores stores, EruptUserService userService) {
        this.stores = stores;
        this.userService = userService;
    }

    // Every endpoint here belongs to the page's menu
    @Override
    public String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
        return FileCenterTpl.PAGE;
    }

    private void writable() {
        if (null == userService.getEruptMenuByValue(FileCenterTpl.WRITE)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.read_only"));
    }

    private static <T> T io(IoCall<T> call) {
        try {
            return call.call();
        } catch (IllegalArgumentException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.io_failed") + " → " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface IoCall<T> {
        T call() throws IOException;
    }

    @GetMapping("/stores")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<List<String>> stores() {
        return R.ok(stores.names());
    }

    @GetMapping("/list")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<List<FileEntry>> list(@RequestParam("store") String store, @RequestParam(value = "path", defaultValue = "/") String path) {
        return R.ok(io(() -> stores.get(store).list(path)));
    }

    @GetMapping("/stat")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<Map<String, Object>> stat(@RequestParam("store") String store, @RequestParam("path") String path) {
        FileStore fileStore = stores.get(store);
        FileEntry entry = io(() -> fileStore.stat(path));
        if (null == entry) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.not_found") + " → " + path);
        return R.ok(Map.of("entry", entry, "url", String.valueOf(fileStore.publicUrl(path))));
    }

    /**
     * The file itself, for previews and downloads. Opened by the browser, so the token travels as
     * the _token parameter.
     */
    @GetMapping("/raw")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void raw(@RequestParam("store") String store, @RequestParam("path") String path,
                    @RequestParam(value = "download", required = false) Boolean download, HttpServletResponse response) {
        FileStore fileStore = stores.get(store);
        FileEntry entry = io(() -> fileStore.stat(path));
        if (null == entry || entry.dir()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.not_found") + " → " + path);
        response.setContentType(null == entry.contentType() ? "application/octet-stream" : entry.contentType());
        if (entry.size() > 0) response.setContentLengthLong(entry.size());
        String name = URLEncoder.encode(entry.name(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, (Boolean.TRUE.equals(download) ? "attachment" : "inline") + "; filename*=UTF-8''" + name);
        io(() -> {
            try (InputStream in = fileStore.read(path)) {
                in.transferTo(response.getOutputStream());
            }
            response.flushBuffer();
            return null;
        });
    }

    @PostMapping("/upload")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<Integer> upload(@RequestParam("store") String store, @RequestParam(value = "path", defaultValue = "/") String dir,
                             @RequestParam("files") MultipartFile[] files) {
        this.writable();
        FileStore fileStore = stores.get(store);
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            if (null == name || name.isBlank()) continue;
            io(() -> {
                try (InputStream in = file.getInputStream()) {
                    fileStore.write(FileStore.join(dir, FileStore.nameOf(name)), in, file.getSize(), file.getContentType());
                }
                return null;
            });
        }
        return R.ok(files.length);
    }

    @PostMapping("/mkdir")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<Void> mkdir(@RequestParam("store") String store, @RequestParam("path") String dir, @RequestParam("name") String name) {
        this.writable();
        io(() -> {
            stores.get(store).mkdir(FileStore.join(dir, name));
            return null;
        });
        return R.ok();
    }

    @PostMapping("/rename")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<Void> rename(@RequestParam("store") String store, @RequestParam("path") String path, @RequestParam("name") String name) {
        this.writable();
        FileStore.join("/", name);
        io(() -> {
            stores.get(store).rename(path, name);
            return null;
        });
        return R.ok();
    }

    @PostMapping("/delete")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = FileCenterController.class)
    public R<Void> delete(@RequestParam("store") String store, @RequestBody List<String> paths) {
        this.writable();
        FileStore fileStore = stores.get(store);
        for (String path : paths) {
            io(() -> {
                fileStore.delete(path);
                return null;
            });
        }
        return R.ok();
    }

}
