package xyz.erupt.core.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class ScriptUtilTest {

    @Test
    public void scriptTest() {
        /* ---------------- boolean ---------------- */
        assert ScriptUtil.eval("a==b", Map.of("a", "x", "b", "x")).asBoolean();
        assert !ScriptUtil.eval("a!=b", Map.of("a", 1, "b", 1)).asBoolean();
        assert ScriptUtil.eval("!(a<b)", Map.of("a", 2, "b", 1)).asBoolean();
        /* ---------------- number ---------------- */
        assert ScriptUtil.eval("a+b", Map.of("a", 1, "b", 1)).asInt() == 2;
        assert ScriptUtil.eval("a*b/2", Map.of("a", 4.0, "b", 3)).asDouble() == 6.0;
        assert ScriptUtil.eval("Math.abs(a)", Map.of("a", -7)).asInt() == 7;
        assert Objects.equals(ScriptUtil.eval("pie.toFixed(2)", Map.of("pie", 3.1415026)).asString(), "3.14");

        /* ---------------- string ---------------- */
        assert "HelloWorld".equals(ScriptUtil.eval("a+b", Map.of("a", "Hello", "b", "World")).asString());
        assert ScriptUtil.eval("a.length", Map.of("a", "abc")).asInt() == 3;
        assert ScriptUtil.eval("a.startsWith('Hi')", Map.of("a", "Hi there")).asBoolean();

        /* ---------------- date ---------------- */
        assert ScriptUtil.eval("new Date().getMonth() + 1", null).asInt() == LocalDate.now().getMonthValue();

        /* ---------------- object ---------------- */
        assert ScriptUtil.eval("var list = [1,2,3]; list.length == 3", null).asBoolean();
        assert ScriptUtil.eval("let map = {k: 100}; map['k']", null).asInt() == 100;

        /* ---------------- function ---------------- */
        assert ScriptUtil.eval("var add = (x,y) => x+y; add(a, b)",
                Map.of("a", 10, "b", 20)).asInt() == 30;
    }


}
