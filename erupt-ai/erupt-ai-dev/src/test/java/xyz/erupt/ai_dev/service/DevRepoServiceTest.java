package xyz.erupt.ai_dev.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DevRepoServiceTest {

    @Test
    void hostOfEveryUrlForm() {
        assertEquals("github.com", DevRepoService.host("https://github.com/erupts/erupt.git"));
        assertEquals("git.example.com", DevRepoService.host("https://deploy@git.example.com:8443/team/web.git"));
        assertEquals("gitlab.example.com", DevRepoService.host("ssh://git@GitLab.Example.com:2222/team/web.git"));
        assertEquals("github.com", DevRepoService.host("git@github.com:erupts/erupt.git"));
        assertEquals("::1", DevRepoService.host("ssh://git@[::1]:22/x.git"));
        assertNull(DevRepoService.host("/srv/git/web.git"), "a local path has no host");
        assertNull(DevRepoService.host("file:///srv/git/web.git"));
        assertNull(DevRepoService.host(" "));
    }

    @Test
    void aCredentialGoesOnlyWhereItIsListed() {
        assertTrue(DevRepoService.allowed(List.of("github.com"), "github.com"));
        assertFalse(DevRepoService.allowed(List.of("github.com"), "evil.example.com"));
        assertFalse(DevRepoService.allowed(List.of("github.com"), "github.com.evil.example.com"), "a longer name is another host");
        assertTrue(DevRepoService.allowed(List.of("*.example.com"), "git.example.com"));
        assertFalse(DevRepoService.allowed(List.of("*.example.com"), "example.com"), "the pattern means subdomains only");
        assertFalse(DevRepoService.allowed(List.of("*.example.com"), "notexample.com"));
        assertTrue(DevRepoService.allowed(List.of("*"), null), "an explicit * allows any host");
        assertFalse(DevRepoService.allowed(List.of(), "github.com"), "none listed: never used");
        assertFalse(DevRepoService.allowed(null, "github.com"));
    }

}
