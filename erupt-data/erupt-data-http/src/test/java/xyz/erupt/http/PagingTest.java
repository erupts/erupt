package xyz.erupt.http;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.view.Page;
import xyz.erupt.http.annotation.EruptHttp;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author YuePeng
 */
public class PagingTest {

    private Map<String, String> apply(EruptHttp.Paging paging) {
        Map<String, String> params = new LinkedHashMap<>();
        paging.apply(params, new Page(3, 20));
        return params;
    }

    @Test
    void conventionsWriteTheirOwnParameterNames() {
        assertEquals(Map.of("pageIndex", "3", "pageSize", "20"), apply(EruptHttp.Paging.PAGE_SIZE));
        assertEquals(Map.of("page", "3", "per_page", "20"), apply(EruptHttp.Paging.PAGE_PER_PAGE));
        assertEquals(Map.of("offset", "40", "limit", "20"), apply(EruptHttp.Paging.OFFSET_LIMIT));
    }

}
