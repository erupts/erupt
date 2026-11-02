package xyz.erupt.revision.pojo;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One field of one revision: the field name, its form title at the time of the change, and the
 * masked JSON value on each side. {@code before} is absent on an ADD, {@code after} on a DELETE.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    private String field;

    private String title;

    private JsonElement before;

    private JsonElement after;

}
