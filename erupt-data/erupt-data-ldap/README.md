# erupt-data-ldap

LDAP directory data source for Erupt. Bind a `@Erupt` model to entries in Active Directory, OpenLDAP, FreeIPA, ApacheDS, or any RFC 4511 server — Erupt then manages those entries like rows in a database table.

Uses the JDK's built-in JNDI provider — no extra runtime dependency beyond `erupt-core`.

## Annotation

`@EruptLdap`

| Attribute        | Default              | Description                                                                     |
|------------------|----------------------|---------------------------------------------------------------------------------|
| `url`            | —                    | LDAP server URL, e.g. `ldap://ldap.example.com:389` or `ldaps://...`            |
| `baseDn`         | —                    | Search base DN, e.g. `ou=people,dc=example,dc=com`                              |
| `rdn`            | `"cn"`               | RDN attribute used to build the entry DN, e.g. `uid`, `cn`                      |
| `filter`         | `"(objectClass=*)"`  | Base LDAP filter narrowing the search                                            |
| `objectClasses`  | `{}`                 | Object classes assigned when creating a new entry — empty disables writes       |
| `bindDn`         | `""`                 | Bind DN for authentication; empty for anonymous bind                             |
| `bindCredential` | `""`                 | Bind credential (password)                                                       |
| `attributes`     | `{}`                 | Attributes fetched from the directory; empty returns every attribute            |
| `sizeLimit`      | `500`                | Maximum entries returned by a single search                                      |
| `timeout`        | `10`                 | Search time limit in seconds                                                     |

Field names on the model = LDAP attribute names (case-insensitive). The primary key column supplies the RDN value; entry DN = `{rdn}={id},{baseDn}`.

## Example — inetOrgPerson

```java
@Getter
@Setter
@Erupt(name = "Directory User", primaryKeyCol = "uid")
@EruptLdap(
    url = "ldap://ldap.example.com:389",
    baseDn = "ou=people,dc=example,dc=com",
    rdn = "uid",
    filter = "(objectClass=inetOrgPerson)",
    objectClasses = { "inetOrgPerson", "top" },
    bindDn = "cn=admin,dc=example,dc=com",
    bindCredential = "secret"
)
@EruptDataProcessor(EruptLdapDataService.DATA_PROCESSOR)
public class DirectoryUser {

    @EruptField(views = @View(title = "UID"))
    private String uid;

    @EruptField(
        views = @View(title = "Full Name"),
        edit = @Edit(title = "Full Name", notNull = true, search = @Search(vague = true))
    )
    private String cn;

    @EruptField(edit = @Edit(title = "Surname", notNull = true))
    private String sn;

    @EruptField(edit = @Edit(title = "Email"))
    private String mail;

    @EruptField(edit = @Edit(title = "Phone"))
    private String telephoneNumber;
}
```

## Operations

Full CRUD:

- **List**: search by `filter` combined with pushed-down `EQ` / `LIKE` / `NULL` / `NOT_NULL` conditions (RFC 4515 escaped). The base engine re-evaluates all conditions for exact semantics.
- **Find by id**: direct DN lookup via `getAttributes(dn)`.
- **Add**: `createSubcontext` with the declared `objectClasses` and all non-null model fields.
- **Edit**: `modifyAttributes` (skips the RDN attribute; renaming DN is a separate op).
- **Delete**: `destroySubcontext(dn)`.

Leave `objectClasses` empty to make the model read-only.

## Gotchas

- Binary attributes (`jpegPhoto`, `userCertificate;binary`) are decoded as UTF-8 strings — model these as `String` and expect base64-ish content, or exclude them via `attributes = { ... }`.
- Multi-valued attributes (e.g. `memberOf`, `mail;alt`) come back as `List<Object>`; declare the field as `List<String>`.
- The primary key value is used verbatim as the RDN value; changing it on edit requires a `rename`, which this module does not perform.
