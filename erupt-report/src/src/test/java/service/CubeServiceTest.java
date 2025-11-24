package service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import xyz.erupt.bi.cube.pojo.query.CubeQuery;
import xyz.erupt.bi.cube.pojo.query.SqlParameter;
import xyz.erupt.bi.cube.service.CubeQueryService;

public class CubeServiceTest {

    @Resource
    private CubeQueryService cubeQueryService;

    @Test
    public void test() {
        CubeQuery cubeQuery = new CubeQuery();
        cubeQuery.setCubeName("cube");
        cubeQuery.setGroupBy(true);
        SqlParameter cubeSql = cubeQueryService.cubeToSql(cubeQuery);
    }

}
