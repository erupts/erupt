package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;

import java.util.List;

/**
 * What a repository is asked for. Condition keys and sort fields are columns already checked against the model,
 * so a repository may use them as identifiers; values must always travel as parameters.
 *
 * @param conditions values are stored values (a reference's key): a list of two for {@code RANGE}, a list for
 *                   {@code IN} / {@code NOT_IN}, the text to look for (no wildcards) for {@code LIKE} /
 *                   {@code NOT_LIKE}, null for {@code NULL} / {@code NOT_NULL}; never {@code AUTO}
 * @param sort       most significant first; empty means primary key ascending
 * @param filters    SQL the model's {@code DataProxy.beforeFetch} returned, ANDed as written; a repository that
 *                   cannot run SQL refuses a query that has any rather than ignore them
 */
public record Query(List<Condition> conditions, List<Sort> sort, int offset, int limit, List<String> filters) {

    public Query(List<Condition> conditions, List<Sort> sort, int offset, int limit) {
        this(conditions, sort, offset, limit, List.of());
    }

}
