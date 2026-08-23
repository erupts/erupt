package xyz.erupt.ldap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to entries in an LDAP directory. Place alongside
 * {@code @EruptDataProcessor(EruptLdapDataService.DATA_PROCESSOR)}.
 * <p>
 * Field names on the erupt model must match LDAP attribute names (case-insensitive).
 * The model's primary key column supplies the RDN value; the entry DN is built as
 * {@code {rdn}={id},{baseDn}}. Writes require {@link #objectClasses()} to declare
 * the object classes the entry should be created with.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptLdap {

    /**
     * LDAP server URL, e.g. {@code ldap://ldap.example.com:389} or {@code ldaps://...}.
     */
    String url();

    /**
     * Search base DN, e.g. {@code ou=people,dc=example,dc=com}.
     */
    String baseDn();

    /**
     * RDN attribute used to build the entry DN, e.g. {@code uid} or {@code cn}.
     */
    String rdn() default "cn";

    /**
     * Base LDAP filter narrowing the search, e.g. {@code (objectClass=inetOrgPerson)}.
     */
    String filter() default "(objectClass=*)";

    /**
     * Object classes assigned when creating a new entry. Empty disables writes.
     */
    String[] objectClasses() default {};

    /**
     * Bind DN for authentication. Empty for anonymous bind.
     */
    String bindDn() default "";

    /**
     * Bind credential (password). Empty for anonymous bind.
     */
    String bindCredential() default "";

    /**
     * Attributes fetched from the directory. Empty returns every attribute.
     */
    String[] attributes() default {};

    /**
     * Maximum entries returned by a single search.
     */
    int sizeLimit() default 500;

    /**
     * Search time limit in seconds. 0 disables the limit.
     */
    int timeout() default 10;

}
