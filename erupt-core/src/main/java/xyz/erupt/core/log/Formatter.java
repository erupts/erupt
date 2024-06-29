package xyz.erupt.core.log;

import org.fusesource.jansi.Ansi;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author YuePeng
 * date 2024/6/27 21:58
 */
public class Formatter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final StringBuilder buf = new StringBuilder();

    public Formatter timestamp(Long timestamp) {
        synchronized (DATE_FORMAT) {
            buf.append(DATE_FORMAT.format(timestamp));
        }
        return this;
    }

    public Formatter space() {
        buf.append(" ");
        return this;
    }

    public Formatter level(String level) {
        Ansi ansi;
        if (Level.INFO.name().equals(level)) {
            ansi = ansi().fg(Ansi.Color.GREEN);
        } else if (Level.WARN.name().equals(level)) {
            ansi = ansi().fg(Ansi.Color.YELLOW);
        } else if (Level.ERROR.name().equals(level)) {
            ansi = ansi().fg(Ansi.Color.RED);
        } else {
            ansi = ansi().fg(Ansi.Color.CYAN);
        }
        buf.append(ansi);
        this.alignment(level, 5);
        buf.append(ansi().fg(Ansi.Color.DEFAULT));
        return this;
    }

    public Formatter value(String value) {
        buf.append(value);
        return this;
    }

    public Formatter thread(String value) {
        this.alignment(value, 15);
        return this;
    }

    public Formatter name(String name) {
        buf.append(ansi().fg(Ansi.Color.CYAN));
        this.alignment(name, 40);
        buf.append(ansi().fg(Ansi.Color.DEFAULT));
        return this;
    }

    public Formatter newline() {
        buf.append("\n");
        return this;
    }

    public Formatter throwable(Throwable throwable) {
        if (throwable != null) {
            this.newline();
            StringWriter sw = new StringWriter(1024);
            PrintWriter writer = new PrintWriter(sw);
            throwable.printStackTrace(writer);
            writer.close();
            buf.append(sw.getBuffer());
            this.newline();
        }
        return this;
    }

    public void alignment(String text, int size) {
        if (text.length() < size) {
            for (int i = size - text.length(); i > 0; i--) {
                buf.append(" ");
            }
            buf.append(text);
        } else {
            buf.append(text.substring(text.length() - size));
        }
    }


    @Override
    public String toString() {
        return buf.toString();
    }

}
