package com.erupt.exception;

import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class ExceptionUtil {

    public static EruptFieldAnnotationException styleEruptException(EruptFieldModel eruptFieldModel, String message) {
        return new EruptFieldAnnotationException(
                ansi().fg(Ansi.Color.RED).a(message)
                        + ansi().fgBright(Ansi.Color.BLUE).fg(Ansi.Color.BLUE).
                        a("(" + eruptFieldModel.getField().getDeclaringClass().getName() + "."
                                + eruptFieldModel.getField().getName() + ")").toString()
                        + ansi().fgBright(Ansi.Color.RED).a("")
        );
    }

    public static String styleEruptException(EruptModel eruptModel, String message) {
        throw new EruptAnnotationException(ansi().fg(Ansi.Color.RED).a(
                eruptModel.getClazz().getName() +
                        "---------->" + message
        ).toString());

    }
}
