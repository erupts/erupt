package xyz.erupt.example;

import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
public class test {

    public static boolean containsSqlInjection(Object obj) {
        Pattern pattern = Pattern.compile("(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)");

        Matcher matcher = pattern.matcher(obj.toString());
        return matcher.find();
    }

    public static void main(String[] args) throws ScriptException {
        boolean b1 = containsSqlInjection("and nm=1");
        System.out.println("b1:" + b1);
        boolean b2 = containsSqlInjection("niamsh delete from ");
        System.out.println("b2:" + b2);
        boolean b3 = containsSqlInjection("stand");
        System.out.println("b3:" + b3);
        boolean b4 = containsSqlInjection("and");
        System.out.println("b4:" + b4);
        boolean b5 = containsSqlInjection("niasdm%asjdj");
        System.out.println("b5:" + b5);


    }
}
