# erupt-config

Runtime settings inside the admin: key/value items per Spring profile, layered the way `application-{profile}.yml` layers over `application.yml`. The items are a Spring property source, so `@Value("${key}")`, `Environment#getProperty` and `@ConfigurationProperties` read them like any file, and a saved change reaches the running beans without a restart.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-config</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

```yaml
spring:
  profiles:
    active: prod           # the profiles this instance reads, on top of default; nothing erupt-specific
erupt:
  config:
    refresh-seconds: 30    # how often another instance's changes are picked up; 0 turns polling off
```

## Page

Profiles on the left, the picked profile's keys on the right. `default` is read by every instance; a profile named in `spring.profiles.active` is read on top of it, so a profile holds only what differs. Profiles are added, renamed and removed in the tree; `default` and a profile that still has items stay. The running instance's profiles are created on first start.

A secret item is masked in the table and still read by the application. An item switched off stays in the table and leaves the application: the files' value applies again. The Value cell is drawn by type: a swatch, the icon, the picture for an image, a link for an attachment, tags for a list, the whole text in a box that scrolls. *Copy to profile* copies selected keys elsewhere. Item types: string, password (always a secret), text, number, boolean, list (comma-separated, binds to a `List`), key/value, JSON, YAML, date, datetime, time, color, icon, HTML, image and attachment (the property of an upload is its path under `erupt.upload-path`). Key/value, JSON and YAML are flattened into `key.path` and `key[i]` properties the way Spring reads a file, so a `Map`, a `List` or a `@ConfigurationProperties` bean binds to them; JSON and YAML keep the raw text under `key` too.

**Effective values** is the merged result this instance reads right now, one row per property with the profile it came from; *Refresh now* reloads this instance from the database.

## How values reach the application

1. **Before the context starts**, `EruptConfigEnvironmentPostProcessor` reads the layered profiles with plain JDBC through `spring.datasource.*` and puts the merged map in front of every file. A database without the tables yet is skipped: first boot runs on the files' values.
2. **At runtime**, saving an item refreshes this instance: the property source is replaced, every singleton's `@Value("${...}")` field is set again, and `EruptConfigRefreshedEvent` is published with the keys that changed. Other instances notice within `refresh-seconds`.

```java
@Value("${app.mail.host:localhost}")   // follows a change
private String mailHost;

@Resource
private EruptConfig config;
config.get("app.mail.host");
config.get(Keys.MAIL_PORT, Integer.class, 25);   // an enum whose toString is the key

@EventListener
void on(EruptConfigRefreshedEvent event) { event.getKeys(); }
```

Precedence: active profile > `default` > files. A key missing from the database falls through to the files as before. SpEL expressions (`#{...}`) and setter injection are resolved once at startup only.

Not a data dictionary: `EruptDict` answers *which values a field may take* and feeds dropdowns; this module answers *what a parameter is right now* and feeds code.
