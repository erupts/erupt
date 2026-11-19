package xyz.erupt.test.core;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.config.GsonFactory;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * LocalTime crosses the wire as the time picker's HH:mm:ss. Without a registered adapter Gson
 * reflects into java.time, which the module system refuses, and a form holding a TIME field
 * fails to save.
 */
class GsonTimeTest {

    private final Gson gson = GsonFactory.getGson();

    record Holder(LocalTime time) {
    }

    @Test
    void localTimeRoundTripsAsHourMinuteSecond() {
        assertEquals("{\"time\":\"08:30:00\"}", gson.toJson(new Holder(LocalTime.of(8, 30))));
        assertEquals(LocalTime.of(8, 30), gson.fromJson("{\"time\":\"08:30:00\"}", Holder.class).time());
        assertEquals(LocalTime.of(22, 0), gson.fromJson("{\"time\":\"22:00\"}", Holder.class).time());
    }

}
