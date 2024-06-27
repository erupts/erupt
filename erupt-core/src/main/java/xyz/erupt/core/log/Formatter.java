package xyz.erupt.core.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

public class Formatter {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public final StringBuilder buf = new StringBuilder();

    public Formatter timestamp(Long timestamp) {
        buf.append(DATE_FORMAT.format(timestamp));
        return this;
    }

    public Formatter space(){
        buf.append(" ");
        return this;
    }

    public Formatter value(String value) {
        buf.append(value);
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

    @Override
    public String toString() {
        return buf.toString();
    }
}
