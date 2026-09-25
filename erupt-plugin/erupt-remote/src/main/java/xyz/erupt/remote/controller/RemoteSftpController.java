package xyz.erupt.remote.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.model.SftpEntry;
import xyz.erupt.remote.service.RemoteHostAccess;
import xyz.erupt.remote.service.RemoteSftpService;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * File panel of the SSH terminal page. Every call re-checks the host the same way the ticket endpoint does, so a
 * host that was disabled, had file transfer switched off or lost this user after the page opened is refused on
 * the next click.
 * <p>
 * Uploads are the raw request body, not multipart: the bytes stream straight into the SFTP channel with no temp
 * file and no multipart size ceiling. Downloads are streamed with {@code _token} in the URL, as attachments are.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/remote/sftp")
public class RemoteSftpController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RemoteHostAccess remoteHostAccess;

    @Resource
    private RemoteSftpService sftpService;

    @GetMapping("/{id}/home")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<String> home(@PathVariable("id") Long id) {
        return R.ok(sftpService.home(host(id)));
    }

    @GetMapping("/{id}/ls")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<List<SftpEntry>> ls(@PathVariable("id") Long id, @RequestParam("path") String path) {
        return R.ok(sftpService.list(host(id), path));
    }

    @GetMapping("/{id}/download")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public void download(@PathVariable("id") Long id, @RequestParam("path") String path, HttpServletResponse response) throws IOException {
        RemoteHost host = host(id);
        String file = RemoteSftpService.normalize(path);
        String name = file.substring(file.lastIndexOf('/') + 1);
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        sftpService.download(host, file, size -> {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Length", String.valueOf(size));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
            try {
                return response.getOutputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        response.flushBuffer();
    }

    @PostMapping("/{id}/upload")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<Void> upload(@PathVariable("id") Long id, @RequestParam("path") String dir, @RequestParam("name") String name,
                          HttpServletRequest request) throws IOException {
        try (InputStream in = request.getInputStream()) {
            sftpService.upload(host(id), dir, name, in);
        }
        return R.ok();
    }

    @PostMapping("/{id}/mkdir")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<Void> mkdir(@PathVariable("id") Long id, @RequestParam("path") String dir, @RequestParam("name") String name) {
        sftpService.mkdir(host(id), dir, name);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @EruptMenuAuth(RemoteHost.MENU_VALUE)
    public R<Void> delete(@PathVariable("id") Long id, @RequestParam("path") String path) {
        sftpService.delete(host(id), path);
        return R.ok();
    }

    private RemoteHost host(Long id) {
        RemoteHost host = eruptDao.find(RemoteHost.class, id);
        Erupts.requireNonNull(host, I18nTranslate.$translate("remote.host_not_found"));
        Erupts.requireTrue(Boolean.TRUE.equals(host.getEnabled()), I18nTranslate.$translate("remote.host_disabled"));
        Erupts.requireTrue(remoteHostAccess.canOpen(host.getId()), I18nTranslate.$translate("remote.not_authorized"));
        Erupts.requireTrue(RemoteHost.PROTOCOL_SSH.equals(host.getProtocol()) && Boolean.TRUE.equals(host.getFileTransfer()),
                I18nTranslate.$translate("remote.sftp_disabled"));
        return host;
    }
}
