package xyz.erupt.exception;

import xyz.erupt.base.model.EruptFieldModel;
import xyz.erupt.base.model.EruptModel;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class ExceptionUtil {

    public static EruptFieldAnnotationException styleEruptFieldException(EruptFieldModel eruptFieldModel, String message) {
        return new EruptFieldAnnotationException(
                ansi().fg(Ansi.Color.RED).a(message).fgBright(Ansi.Color.BLUE).fg(Ansi.Color.BLUE)
                        .a("(" + eruptFieldModel.getField().getDeclaringClass().getName() + "."
                                + eruptFieldModel.getField().getName() + ")").reset().toString()

        );
    }

    public static EruptAnnotationException styleEruptException(EruptModel eruptFieldModel, String message) {
        return new EruptAnnotationException(
                ansi().fg(Ansi.Color.RED).a(message)
                        + ansi().fgBright(Ansi.Color.BLUE).fg(Ansi.Color.BLUE).
                        a("(" + eruptFieldModel.getClazz().getName() + ")").reset().toString()
        );
    }

}
