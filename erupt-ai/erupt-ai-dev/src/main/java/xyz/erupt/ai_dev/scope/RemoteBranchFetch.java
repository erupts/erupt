package xyz.erupt.ai_dev.scope;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.annotation.fun.AutoCompleteHandler;
import xyz.erupt.git.EruptGit;
import xyz.erupt.git.GitException;
import xyz.erupt.git.model.GitRemote;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Branch names of the repository being edited, read from its remote with the credential it names: the
 * default branch first, then the rest, filtered by what is typed. A name not on the list can still be typed.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class RemoteBranchFetch implements AutoCompleteHandler<DevProjectRepo> {

    private static final int MAX = 100;

    // every keystroke asks again: the remote is read at most once a minute per repository
    private static final long CACHE_MILLIS = 60_000;

    private record Cached(long at, GitRemote remote) {
    }

    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    @Resource
    private EruptGit eruptGit;

    @Resource
    private DevRepoService devRepoService;

    @Override
    public List<Object> completeHandler(DevProjectRepo repo, String val, String[] param) {
        if (null == repo || StringUtils.isBlank(repo.getUrl())) return List.of();
        GitRemote remote;
        try {
            remote = this.remote(repo.getUrl().trim(), StringUtils.trimToNull(repo.getCredential()));
        } catch (GitException e) {
            // an unreachable or mistyped remote offers nothing; the branch can still be typed
            return List.of();
        }
        String typed = StringUtils.trimToEmpty(val).toLowerCase(Locale.ROOT);
        List<Object> names = new ArrayList<>();
        if (null != remote.defaultBranch() && remote.defaultBranch().toLowerCase(Locale.ROOT).contains(typed)) names.add(remote.defaultBranch());
        for (String branch : remote.branches()) {
            if (names.size() >= MAX) break;
            if (!branch.equals(remote.defaultBranch()) && branch.toLowerCase(Locale.ROOT).contains(typed)) names.add(branch);
        }
        return names;
    }

    private GitRemote remote(String url, String credential) {
        String key = url + "\n" + StringUtils.defaultString(credential);
        long now = System.currentTimeMillis();
        Cached cached = cache.get(key);
        if (null != cached && now - cached.at() < CACHE_MILLIS) return cached.remote();
        GitRemote remote = eruptGit.remote(url, devRepoService.auth(credential, url));
        cache.put(key, new Cached(now, remote));
        return remote;
    }

}
