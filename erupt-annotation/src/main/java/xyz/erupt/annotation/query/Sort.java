package xyz.erupt.annotation.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2025/10/30 22:40
 */
@Getter
@Setter
@NoArgsConstructor
public class Sort {

    private String field;

    private Direction direction = Direction.asc;

    public enum Direction {
        asc,
        desc
    }

    public Sort(String field, Direction direction) {
        this.field = field;
        this.direction = direction;
    }

    public static String toSortString(List<Sort> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return null;
        }
        return sorts.stream()
                .map(s -> s.getField() + " " + s.getDirection().name().toLowerCase())
                .collect(Collectors.joining(", "));
    }

    public static List<Sort> toSortList(String orderBy) {
        List<Sort> result = new ArrayList<>();
        if (orderBy == null || (orderBy = orderBy.trim()).isEmpty()) return result;
        String[] segments = orderBy.split(",");
        for (String seg : segments) {
            if ((seg = seg.trim()).isEmpty()) continue;
            result.add(parseOne(seg));
        }
        return result;
    }

    private static Sort parseOne(String seg) {
        seg = seg.replaceAll("\\s+", " ");
        int sp = seg.lastIndexOf(' ');
        String field, dirPart;
        if (sp == -1) {
            field = seg;
            dirPart = "ASC";
        } else {
            field = seg.substring(0, sp).trim();
            dirPart = seg.substring(sp + 1).trim();
        }

        if (field.isEmpty()) {
            throw new IllegalArgumentException("Field name must not be empty");
        }

        Direction direction = switch (dirPart.toUpperCase()) {
            case "ASC" -> Direction.asc;
            case "DESC" -> Direction.desc;
            default -> throw new IllegalArgumentException("Sort direction must be ASC or DESC, but was: " + dirPart);
        };
        return new Sort(field, direction);
    }

}
