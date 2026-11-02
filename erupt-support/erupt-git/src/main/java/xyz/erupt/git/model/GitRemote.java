package xyz.erupt.git.model;

import java.util.List;

/**
 * What a remote offers, read without cloning it.
 *
 * @param defaultBranch the branch its HEAD points at, null when it has none (an empty repository)
 * @param branches      its branch names, sorted
 * @author YuePeng
 * date 2026/10/23
 */
public record GitRemote(String defaultBranch, List<String> branches) {
}
