# Security Policy

Erupt generates admin backends that sit in front of your database and user directory,
so we treat authentication, authorization, SQL/JPQL construction and file handling as
security-sensitive. Please report issues in those areas privately.

## Supported versions

| Version | Supported |
|---------|-----------|
| 2.x     | Yes       |
| 1.x     | No, please upgrade |

Only the latest 2.x release receives security fixes.

## Reporting a vulnerability

**Do not open a public issue.**

1. Preferred: [report privately on GitHub](https://github.com/erupts/erupt/security/advisories/new)
   (Security tab, "Report a vulnerability").
2. Alternative: email **erupts@126.com** with `[SECURITY]` in the subject.

Please include the Erupt version, the affected module (e.g. `erupt-upms`, `erupt-core`,
`erupt-ai`), a minimal reproduction and the impact you see. Remove any real credentials
or production data from logs before sending.

## What to expect

- Acknowledgement within 3 business days.
- A fix or mitigation targeted within 30 days for confirmed high-severity issues, released
  as a patch version and announced in the [changelog](https://docs.erupt.xyz/guide/changelog).
- Credit in the release notes unless you prefer to stay anonymous.

## Scope

In scope: everything published under `xyz.erupt` on Maven Central and the `erupts/erupt`
Docker image. Out of scope: the demo site and third-party services Erupt integrates with
(LLM providers, Feishu, Notion, etc.), which should be reported to their respective vendors.
