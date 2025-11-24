package xyz.erupt.bi.cube;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.cube.annotation.*;
import xyz.erupt.bi.cube.constant.SqlType;

@Getter
@Setter
@Cube(
        title = "测试",
        sql = """
                      select * from t_user
                      where 1=1
                      #if($a)
                        and name like concat('%', :a, '%')
                      #end
                      #if($b)
                        and gender = :gender
                      #end
                      #if($ids && $ids.size() > 0)
                        and id in (:ids)
                      #end
                      ;
                """,
        explores = {
                @Explore(
                        name = "AA", where = "name = '1'", cacheTime = 100, params = @Explore.Param(name = "a", value = "100")
                )
        }
)
public class CubeJoinTest {

    @Dimension(
            title = "姓名",
            tags = {"a", "b"}
    )
    private String name;

    @Dimension(
            title = "性别"
    )
    private String sex;

    @Measure(
            title = "数量", sql = "count(1)"
    )
    private Integer count;

    @Measure(
            title = "金额", sql = "sum(money)"
    )
    private Double sum;

}
