package xyz.erupt.core.exception;

import org.fusesource.jansi.Ansi;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author liyuepeng
 * @date 11/1/18.
 */
public class ExceptionUtil {

//    public static final String ANNOTATION_PARSE_ERR_STR = "注解解析异常，请检查注解值是否带有{ @ \\ # : / 等特殊符号，如存在请使用单引号包裹，以解决此问题";

    public static EruptFieldAnnotationException styleEruptFieldException(EruptFieldModel eruptFieldModel, String message) {
        return new EruptFieldAnnotationException(
                ansi().fg(Ansi.Color.RED).a(message).fg(Ansi.Color.BLUE)
                        .a("(" + eruptFieldModel.getField().getDeclaringClass().getName() + "."
                                + eruptFieldModel.getField().getName() + ")").reset().toString()

        );
    }

    public static EruptAnnotationException styleEruptException(EruptModel eruptModel, String message) {
        return new EruptAnnotationException(
                ansi().fg(Ansi.Color.RED).a(message)
                        + ansi().fg(Ansi.Color.BLUE).
                        a("(" + eruptModel.getClazz().getName() + ")").reset().toString()
        );
    }

}
