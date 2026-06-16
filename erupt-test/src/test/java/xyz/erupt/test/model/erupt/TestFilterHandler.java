package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.FilterHandler;

@Component
public class TestFilterHandler implements FilterHandler {

    @Override
    public String filter(String condition, String[] params) {
        // dynamically generates filter conditions, e.g. user-scoped data isolation
        // Return null to apply no extra filter; non-null HQL snippets conflict with
        // Hibernate 6 new-map alias resolution in EruptJpaUtils-generated queries.
        return null;
    }

}
