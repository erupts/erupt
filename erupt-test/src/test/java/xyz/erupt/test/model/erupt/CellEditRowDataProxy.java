package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;

@Component
public class CellEditRowDataProxy implements DataProxy<CellEditRowModel> {

    /**
     * A cross-field rule: neither status nor content is wrong on its own, only the pair is.
     * Reachable from a cell edit only when the whole row is validated.
     */
    @Override
    public void validate(CellEditRowModel model) throws EruptException {
        if ("PUBLISHED".equals(model.getStatus())
                && (null == model.getContent() || model.getContent().isBlank())) {
            throw new EruptException("Published records need content");
        }
    }
}
