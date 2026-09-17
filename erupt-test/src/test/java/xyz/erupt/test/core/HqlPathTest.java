package xyz.erupt.test.core;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptJpaUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sort fields and condition keys land in the hql as identifiers, so anything a client
 * could use to continue the statement has to be refused there.
 */
public class HqlPathTest {

    @Test
    public void acceptsFieldPaths() {
        assertEquals("name", EruptJpaUtils.legalPath("name"));
        assertEquals("dept.name", EruptJpaUtils.legalPath("dept.name"));
        assertEquals("_id$2", EruptJpaUtils.legalPath("_id$2"));
    }

    @Test
    public void refusesStatementFragments() {
        for (String path : new String[]{
                "id desc, (select count(*) from EruptUser)",
                "id) or 1 = 1 --",
                "name' or '1' = '1",
                "id;drop",
                "",
                " ",
                "."}) {
            assertThrows(EruptWebApiRuntimeException.class, () -> EruptJpaUtils.legalPath(path), path);
        }
    }

}
