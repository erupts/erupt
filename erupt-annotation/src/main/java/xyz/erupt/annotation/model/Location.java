package xyz.erupt.annotation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The value of an {@code EditType.MAP} field, independent of the map vendor. Declare the
 * field as this type and the JPA converter stores it as one JSON column; a plain
 * {@code String} field keeps working and holds the same JSON.
 * <p>
 * {@link #crs} names the coordinate system the lng/lat pair is expressed in, so the
 * frontend can convert when the configured vendor uses another one. Values written
 * before the field existed came from AMap and are read as {@link Crs#GCJ02}.
 *
 * @author YuePeng
 */
@Getter
@Setter
@NoArgsConstructor
public class Location {

    private Double lng;

    private Double lat;

    // place name as the vendor returned it
    private String name;

    private String address;

    private Crs crs = Crs.GCJ02;

    public Location(Double lng, Double lat, String name, String address) {
        this.lng = lng;
        this.lat = lat;
        this.name = name;
        this.address = address;
    }

    /**
     * Coordinate reference systems in use by the supported vendors.
     */
    public enum Crs {
        // AMap, Tencent: China's encrypted datum
        GCJ02,
        // Baidu: a further offset on top of GCJ02
        BD09,
        // Google, OpenStreetMap, Tianditu: the GPS datum
        WGS84
    }

}
