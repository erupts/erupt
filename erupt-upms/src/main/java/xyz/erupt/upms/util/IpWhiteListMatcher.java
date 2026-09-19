package xyz.erupt.upms.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches a request IP against a login whitelist whose entries are separated by
 * newlines. Each entry may be an exact IPv4/IPv6 address or an IPv4/IPv6 CIDR
 * block such as {@code 192.168.0.0/24} or {@code 2001:db8::/32}. Both sides are
 * parsed into canonical byte form, so equivalent textual forms (for example
 * {@code ::1} vs {@code 0:0:0:0:0:0:0:1}, or the IPv4-mapped {@code ::ffff:192.168.0.5}
 * vs {@code 192.168.0.5}) compare equal. An empty or null list disables the check.
 *
 * <p>Only strict numeric literals are accepted. Host names, zone-id suffixes
 * (for example {@code fe80::1%eth0}) and IPv4 shorthand such as {@code 127.1}
 * or leading-zero octets never match. Parsing is pure string handling and never
 * performs DNS or other network lookups.
 */
public class IpWhiteListMatcher {

    private IpWhiteListMatcher() {
    }

    public static boolean isAllowed(String ip, String whiteIpList) {
        if (null == whiteIpList || whiteIpList.isBlank()) return true;
        String trimmedIp = null == ip ? null : ip.trim();
        if (null == trimmedIp || trimmedIp.isEmpty()) return false;
        String[] entries = whiteIpList.split("\\R");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (!trimmed.isEmpty() && matches(trimmedIp, trimmed)) return true;
        }
        return false;
    }

    private static boolean matches(String ip, String entry) {
        int slash = entry.indexOf('/');
        String netText = slash < 0 ? entry : entry.substring(0, slash).trim();
        byte[] ipBytes = parseIp(ip);
        byte[] netBytes = parseIp(netText);
        if (null == ipBytes || null == netBytes) return false;
        int maxPrefix = netBytes.length << 3;
        int prefix = maxPrefix;
        if (slash >= 0) {
            try {
                prefix = Integer.parseInt(entry.substring(slash + 1).trim());
            } catch (NumberFormatException e) {
                return false;
            }
            if (prefix < 0 || prefix > maxPrefix) return false;
        }
        if (0 == prefix) return true;
        if (ipBytes.length != netBytes.length) return false;
        int fullBytes = prefix >>> 3;
        for (int i = 0; i < fullBytes; i++) {
            if (ipBytes[i] != netBytes[i]) return false;
        }
        int remainingBits = prefix & 7;
        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits);
            return (ipBytes[fullBytes] & mask) == (netBytes[fullBytes] & mask);
        }
        return true;
    }

    // Strict numeric literal to canonical bytes (4 for IPv4, 16 for IPv6); null when invalid
    private static byte[] parseIp(String ip) {
        if (null == ip || ip.isEmpty() || ip.indexOf('%') >= 0) return null;
        return ip.indexOf(':') >= 0 ? parseIpv6(ip) : parseIpv4(ip);
    }

    // Exactly four decimal octets in 0..255; empty parts, shorthand and leading zeros are rejected
    private static byte[] parseIpv4(String ip) {
        byte[] out = new byte[4];
        int group = 0;
        int start = 0;
        for (int i = 0; i <= ip.length(); i++) {
            if (i == ip.length() || ip.charAt(i) == '.') {
                if (group >= 4 || i == start) return null;
                int octet = 0;
                int digits = 0;
                for (int j = start; j < i; j++) {
                    char c = ip.charAt(j);
                    if (c < '0' || c > '9') return null;
                    octet = octet * 10 + (c - '0');
                    digits++;
                    if (octet > 255) return null;
                }
                if (digits > 3 || (digits > 1 && ip.charAt(start) == '0')) return null;
                out[group++] = (byte) octet;
                start = i + 1;
            }
        }
        return group == 4 ? out : null;
    }

    // Standard textual IPv6: up to eight hex groups, one optional '::' compression
    // and an optional trailing dotted IPv4. IPv4-mapped literals (::ffff:a.b.c.d,
    // also in hex group form) are returned as 4-byte IPv4.
    private static byte[] parseIpv6(String ip) {
        int dbl = ip.indexOf("::");
        if (dbl >= 0 && ip.indexOf("::", dbl + 1) >= 0) return null;
        String head = dbl < 0 ? ip : ip.substring(0, dbl);
        String tail = dbl < 0 ? "" : ip.substring(dbl + 2);
        List<Integer> groups = new ArrayList<>(8);
        // without a compression marker the head holds the whole literal, so its last
        // group may carry the trailing dotted IPv4; with a non-empty tail that role
        // moves to the tail's last group
        if (!parseIpv6Groups(head, groups, tail.isEmpty())) return null;
        int headSize = groups.size();
        if (!parseIpv6Groups(tail, groups, true)) return null;
        int explicit = groups.size();
        if (dbl < 0 ? explicit != 8 : explicit > 7) return null;
        if (dbl >= 0) {
            groups.addAll(headSize, java.util.Collections.nCopies(8 - explicit, 0));
        }
        byte[] out = new byte[16];
        int pos = 0;
        for (int group : groups) {
            out[pos++] = (byte) (group >>> 8);
            out[pos++] = (byte) group;
        }
        // RFC 4291 IPv4-mapped IPv6 prefix (80 zero bits + 16 one bits): treat as plain IPv4
        boolean mapped = true;
        for (int i = 0; i < 10; i++) {
            if (out[i] != 0) {
                mapped = false;
                break;
            }
        }
        if (mapped && out[10] == (byte) 0xFF && out[11] == (byte) 0xFF) {
            return new byte[]{out[12], out[13], out[14], out[15]};
        }
        return out;
    }

    private static boolean parseIpv6Groups(String side, List<Integer> groups, boolean allowDottedV4) {
        if (side.isEmpty()) return true;
        String[] parts = side.split(":", -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) return false;
            int hex = parseHexGroup(part);
            if (hex >= 0) {
                groups.add(hex);
                continue;
            }
            if (allowDottedV4 && i == parts.length - 1 && part.indexOf('.') >= 0) {
                byte[] v4 = parseIpv4(part);
                if (null == v4) return false;
                groups.add((v4[0] & 0xFF) << 8 | (v4[1] & 0xFF));
                groups.add((v4[2] & 0xFF) << 8 | (v4[3] & 0xFF));
                continue;
            }
            return false;
        }
        return true;
    }

    // One to four hex digits; -1 when not a valid hex group
    private static int parseHexGroup(String part) {
        if (part.length() < 1 || part.length() > 4) return -1;
        int value = 0;
        for (int i = 0; i < part.length(); i++) {
            int digit = Character.digit(part.charAt(i), 16);
            if (digit < 0) return -1;
            value = value * 16 + digit;
        }
        return value;
    }
}
