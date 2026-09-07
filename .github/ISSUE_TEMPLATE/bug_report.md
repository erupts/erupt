---
name: Bug report
about: Create a report to help us improve
title: ''
labels: ''
assignees: ''

---

<!--
Usage questions ("how do I ...") belong in Discussions, not here:
https://github.com/erupts/erupt/discussions
-->

### Version information

- JDK version: (e.g. openjdk_17, openjdk_21)
- Erupt version: (please confirm the issue still exists on the latest release)
- Spring Boot version:
- Database: (MySQL / PostgreSQL / Oracle / SQL Server / DM / MongoDB / H2)

### Describe the bug

A clear and concise description of what the bug is.

### To reproduce

Minimal reproduction — the entity plus annotations are usually enough:

```java
@Erupt(name = "xxx")
@Entity
public class Test {

}
```

Steps:

1. ...
2. ...

### Expected behavior

What you expected to happen instead.

### Stack trace / logs

<!-- Please remove tokens, passwords, and any other sensitive information. -->

```
```

### Screenshots

If the problem is in the generated UI, a screenshot helps a lot.

### Additional context

Anything else we should know — custom `DataProxy`, multi-tenant setup, reverse proxy, etc.
