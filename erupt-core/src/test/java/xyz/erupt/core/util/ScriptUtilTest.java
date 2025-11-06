package xyz.erupt.core.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptUtilTest {

    @Test
    public void scriptTest() {
        /* ---------------- boolean ---------------- */
        assert ScriptUtil.eval("a==b", Map.of("a", "x", "b", "x"), boolean.class);
        assert !ScriptUtil.eval("a!=b", Map.of("a", 1, "b", 1), boolean.class);
        assert ScriptUtil.eval("!(a<b)", Map.of("a", 2, "b", 1), boolean.class);
        /* ---------------- number ---------------- */
        assert ScriptUtil.eval("a+b", Map.of("a", 1, "b", 1), double.class) == 2;
        assert ScriptUtil.eval("a*b/2", Map.of("a", 4.0, "b", 3), double.class) == 6.0;
        assert ScriptUtil.eval("Math.abs(a)", Map.of("a", -7), double.class) == 7;
        assert Objects.equals(ScriptUtil.eval("pie.toFixed(2)", Map.of("pie", 3.1415026), String.class), "3.14");

        /* ---------------- string ---------------- */
        assert "HelloWorld".equals(ScriptUtil.eval("a+b", Map.of("a", "Hello", "b", "World"), String.class));
        assert ScriptUtil.eval("a.length", Map.of("a", "abc"), int.class) == 3;
        assert ScriptUtil.eval("a.startsWith('Hi')", Map.of("a", "Hi there"), boolean.class);

        /* ---------------- object ---------------- */
        assert ScriptUtil.eval("var list = [1,2,3]; list.length == 3", Map.of("list", List.of(1, 2, 3)), boolean.class);
        assert ScriptUtil.eval("map['k']", Map.of("map", Map.of("k", 100)), int.class) == 100;

        /* ---------------- date ---------------- */
        assert ScriptUtil.eval("new Date().getMonth() + 1", null, double.class) == LocalDate.now().getMonthValue();

        /* ---------------- function ---------------- */
//        assert ScriptUtil.eval("var add = (x,y) => x+y; add(a, b)",
//                Map.of("a", 10, "b", 20), int.class) == 30;
    }


}
