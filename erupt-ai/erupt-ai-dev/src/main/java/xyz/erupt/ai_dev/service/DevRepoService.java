package xyz.erupt.ai_dev.service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai_dev.constant.RepoStatus;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.git.EruptGit;
import xyz.erupt.git.GitAuth;
import xyz.erupt.git.GitException;
import xyz.erupt.git.GitRepo;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps each project repository's bare clone on the server: cloned on first sync, fetched afterwards,
 * repointed when its URL changes. Syncs run in the background and report through the repository's
 * status fields.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Slf4j
@Service
public class DevRepoService {

    private record RepoRef(Long id, String tenant, String project, String alias, String url, String credential) {
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "erupt-ai-dev-sync");
        thread.setDaemon(true);
        return thread;
    });

    // one git operation per bare repository at a time
    private final Map<Path, Object> locks = new ConcurrentHashMap<>();

    @Resource
    private EruptGit eruptGit;

    @Resource
    private DevLayout devLayout;

    @Resource
    private EruptAiDevProp eruptAiDevProp;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private UpmsTenantSessions upmsTenantSessions;

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Syncs every repository of the project once the current transaction commits, so the background
     * thread reads what was just saved. The sync runs in the caller's tenant.
     */
    public void syncAfterCommit(Long projectId) {
        String tenant = UpmsTenant.normalize(UpmsTenant.tenantId());
        Runnable task = () -> executor.submit(() -> upmsTenantSessions.runBound(tenant, () -> this.sync(projectId)));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    public void sync(Long projectId) {
        List<RepoRef> repos = transactionTemplate.execute(status -> {
            DevProject project = eruptDao.find(DevProject.class, projectId);
            return null == project ? List.<RepoRef>of() : project.getRepos().stream()
                    .map(it -> new RepoRef(it.getId(), project.getTenantId(), project.getCode(), it.getAlias(), it.getUrl(), it.getCredential())).toList();
        });
        for (RepoRef repo : repos) {
            this.mark(repo.id(), RepoStatus.SYNCING, null);
            try {
                this.syncOne(repo);
                this.mark(repo.id(), RepoStatus.READY, null);
            } catch (RuntimeException e) {
                log.warn("Sync of repository {}/{} failed: {}", repo.project(), repo.alias(), e.getMessage());
                this.mark(repo.id(), RepoStatus.FAILED, e.getMessage());
            }
        }
    }

    private void syncOne(RepoRef repo) {
        Path dir = devLayout.repoDir(repo.tenant(), repo.project(), repo.alias());
        GitAuth auth = this.auth(repo.credential(), repo.url());
        synchronized (locks.computeIfAbsent(dir, it -> new Object())) {
            if (!Files.exists(dir)) {
                eruptGit.cloneBare(repo.url(), dir, auth);
                return;
            }
            GitRepo gitRepo = eruptGit.open(dir);
            if (!repo.url().equals(gitRepo.originUrl())) gitRepo.setOriginUrl(repo.url());
            gitRepo.fetch(auth);
        }
    }

    /**
     * The bare clone of a repository, if it has been synced.
     */
    public GitRepo open(DevProject project, String alias) {
        Path dir = devLayout.repoDir(project.getTenantId(), project.getCode(), alias);
        return Files.exists(dir) ? eruptGit.open(dir) : null;
    }

    public GitRepo open(String tenant, String project, String alias) {
        Path dir = devLayout.repoDir(tenant, project, alias);
        if (!Files.exists(dir)) throw new GitException("Repository " + alias + " has not been synced yet");
        return eruptGit.open(dir);
    }

    /**
     * Fetches a synced repository and answers it, so a run starts from the remote's latest state.
     */
    public GitRepo fetch(String tenant, String project, String alias, String credential) {
        GitRepo repo = this.open(tenant, project, alias);
        GitAuth auth = this.auth(credential, repo.originUrl());
        this.locked(tenant, project, alias, () -> repo.fetch(auth));
        return repo;
    }

    /**
     * Runs git work that moves a repository's refs (fetch, push) one at a time per repository.
     */
    public void locked(String tenant, String project, String alias, Runnable work) {
        synchronized (locks.computeIfAbsent(devLayout.repoDir(tenant, project, alias), it -> new Object())) {
            work.run();
        }
    }

    /**
     * Credentials named by a repository, resolved from {@code erupt.ai-dev.credentials}.
     */
    /**
     * The credential of a key, for one remote: refused unless the remote's host is one the credential lists.
     */
    public GitAuth auth(String key, String url) {
        if (StringUtils.isBlank(key)) return GitAuth.NONE;
        EruptAiDevProp.Credential credential = eruptAiDevProp.getCredentials().get(key);
        if (null == credential) throw new GitException("Unknown credential: " + key);
        String host = host(url);
        if (!allowed(credential.getHosts(), host)) {
            throw new GitException("Credential " + key + " is not allowed for host " + (null == host ? "(none)" : host)
                    + "; list it under erupt.ai-dev.credentials." + key + ".hosts");
        }
        if (StringUtils.isNotBlank(credential.getSshKey())) return GitAuth.ssh(Path.of(credential.getSshKey()));
        if (StringUtils.isNotBlank(credential.getToken())) return GitAuth.token(credential.getToken());
        if (StringUtils.isNotBlank(credential.getPassword())) return GitAuth.basic(credential.getUsername(), credential.getPassword());
        throw new GitException("Credential " + key + " has no token, password or ssh-key");
    }

    /**
     * The host of a remote URL: scheme://[user@]host[:port]/path or the scp form user@host:path; null for a
     * local path or anything unreadable.
     */
    static String host(String url) {
        if (StringUtils.isBlank(url)) return null;
        String value = url.trim();
        int scheme = value.indexOf("://");
        String rest;
        if (scheme > 0) {
            rest = value.substring(scheme + 3);
        } else if (value.indexOf(':') > 0 && (value.indexOf('/') < 0 || value.indexOf(':') < value.indexOf('/'))) {
            // scp form: a colon before any slash
            rest = value.substring(0, value.indexOf(':'));
        } else {
            return null;
        }
        int slash = rest.indexOf('/');
        if (slash >= 0) rest = rest.substring(0, slash);
        int at = rest.lastIndexOf('@');
        if (at >= 0) rest = rest.substring(at + 1);
        if (rest.startsWith("[")) {
            int close = rest.indexOf(']');
            return close > 0 ? rest.substring(1, close).toLowerCase(Locale.ROOT) : null;
        }
        int colon = rest.indexOf(':');
        if (colon >= 0) rest = rest.substring(0, colon);
        return rest.isEmpty() ? null : rest.toLowerCase(Locale.ROOT);
    }

    static boolean allowed(List<String> hosts, String host) {
        if (null == hosts) return false;
        for (String pattern : hosts) {
            String p = StringUtils.trimToEmpty(pattern).toLowerCase(Locale.ROOT);
            if (p.equals("*")) return true;
            if (null == host) continue;
            if (p.startsWith("*.") ? host.endsWith(p.substring(1)) : host.equals(p)) return true;
        }
        return false;
    }

    public boolean credentialExists(String key) {
        return eruptAiDevProp.getCredentials().containsKey(key);
    }

    private void mark(Long repoId, RepoStatus status, String message) {
        transactionTemplate.executeWithoutResult(tx -> {
            // a save of the project replaces its repository rows: a row gone meanwhile is simply skipped
            DevProjectRepo repo = eruptDao.find(DevProjectRepo.class, repoId);
            if (null == repo) return;
            repo.setStatus(status);
            repo.setStatusMessage(StringUtils.abbreviate(message, AnnotationConst.REMARK_LENGTH));
            if (status == RepoStatus.READY) repo.setSyncedAt(LocalDateTime.now());
        });
    }

}
