#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};
#end

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;

import java.util.List;

@Component
public class ${NAME} implements DataProxy<${ENTITY}> {

    @Override
    public void beforeAdd(${ENTITY} model) {
    }

    @Override
    public void afterAdd(${ENTITY} model) {
    }

    @Override
    public void beforeUpdate(${ENTITY} model) {
    }

    @Override
    public void afterUpdate(${ENTITY} model) {
    }

    @Override
    public void beforeDelete(${ENTITY} model) {
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        return null;
    }

}
