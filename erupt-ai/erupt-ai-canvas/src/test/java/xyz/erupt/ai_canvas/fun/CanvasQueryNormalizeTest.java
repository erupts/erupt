package xyz.erupt.ai_canvas.fun;

import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.view.TableQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks the normalization the canvas verification tool applies before running
 * a query: erupt paging is 1-BASED (Hibernate rejects negative first-result
 * offsets, so a 0-based index from the LLM must be lifted to 1), the page size
 * is capped for verification, and bare conditions default to EQ like the SDK.
 */
public class CanvasQueryNormalizeTest {

    @Test
    public void zeroBasedPageIndexIsLiftedToOne() {
        TableQuery query = new TableQuery();
        query.setPageIndex(0);
        EruptCanvasModelProvider.normalizeQuery(query);
        assertEquals(1, query.getPageIndex());
    }

    @Test
    public void negativeAndNullPageIndexBecomeOne() {
        TableQuery negative = new TableQuery();
        negative.setPageIndex(-1);
        EruptCanvasModelProvider.normalizeQuery(negative);
        assertEquals(1, negative.getPageIndex());

        TableQuery absent = new TableQuery();
        EruptCanvasModelProvider.normalizeQuery(absent);
        assertEquals(1, absent.getPageIndex());
    }

    @Test
    public void validPageIndexIsKept() {
        TableQuery query = new TableQuery();
        query.setPageIndex(3);
        EruptCanvasModelProvider.normalizeQuery(query);
        assertEquals(3, query.getPageIndex());
    }

    @Test
    public void pageSizeIsCappedForVerification() {
        TableQuery large = new TableQuery();
        large.setPageSize(100);
        EruptCanvasModelProvider.normalizeQuery(large);
        assertEquals(10, large.getPageSize());

        TableQuery absent = new TableQuery();
        EruptCanvasModelProvider.normalizeQuery(absent);
        assertEquals(10, absent.getPageSize());

        TableQuery small = new TableQuery();
        small.setPageSize(5);
        EruptCanvasModelProvider.normalizeQuery(small);
        assertEquals(5, small.getPageSize());
    }

    @Test
    public void bareConditionDefaultsToEqLikeTheSdk() {
        TableQuery query = new TableQuery();
        query.setCondition(List.of(
                new Condition("status", "1", null),
                new Condition("name", "kb", QueryExpression.LIKE)));
        EruptCanvasModelProvider.normalizeQuery(query);
        assertEquals(QueryExpression.EQ, query.getCondition().get(0).getExpression());
        assertEquals(QueryExpression.LIKE, query.getCondition().get(1).getExpression());
    }

}
