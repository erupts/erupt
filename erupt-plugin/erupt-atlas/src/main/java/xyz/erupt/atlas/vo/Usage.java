package xyz.erupt.atlas.vo;

/**
 * A usage as the page reads it: the provider named a target and what it is used for, and the module
 * is read off the provider that answered — the module a binding lives in is the module that knows
 * about it.
 *
 * @author YuePeng
 */
public record Usage(String target, String module, String usage, String owner, String route) {
}
