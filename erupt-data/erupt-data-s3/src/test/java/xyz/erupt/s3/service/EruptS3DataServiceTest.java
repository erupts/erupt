package xyz.erupt.s3.service;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.S3Object;
import xyz.erupt.s3.model.S3ObjectModel;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A listed object is written into the model through typed setters, no field names involved.
 */
public class EruptS3DataServiceTest {

    static class Row extends S3ObjectModel {
    }

    @Test
    public void listedObjectFillsTheModel() {
        Instant modified = Instant.parse("2026-09-24T12:00:00Z");
        S3Object object = S3Object.builder().key("2026-09-24/a.png").size(42L).lastModified(modified)
                .eTag("\"abc\"").storageClass("STANDARD").build();
        S3ObjectModel row = EruptS3DataService.apply(new Row(), object);
        assertEquals("2026-09-24/a.png", row.getId());
        assertEquals(42L, row.getSize());
        assertEquals(Date.from(modified), row.getLastModified());
        assertEquals("\"abc\"", row.getEtag());
        assertEquals("STANDARD", row.getStorageClass());
        assertNull(row.getContentType());
    }

    @Test
    public void missingOptionalAttributesStayNull() {
        S3ObjectModel row = EruptS3DataService.apply(new Row(), S3Object.builder().key("k").build());
        assertEquals("k", row.getId());
        assertNull(row.getLastModified());
        assertNull(row.getStorageClass());
    }

}
