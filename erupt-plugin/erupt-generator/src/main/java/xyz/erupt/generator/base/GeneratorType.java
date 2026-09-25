package xyz.erupt.generator.base;

import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.model.GeneratorField;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Bridges an erupt {@link EditType} with the java type, the jpa annotation and the
 * jdbc column it is generated from.
 *
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Getter
public enum GeneratorType {

    INPUT(EditType.INPUT, "Text Input", String.class.getSimpleName(), "inputType = @InputType"),
    PASSWORD(EditType.PASSWORD, "Password Input", String.class.getSimpleName(), null),
    TEXTAREA(EditType.TEXTAREA, "Textarea", String.class.getSimpleName(), null),
    HTML_EDITOR(EditType.HTML_EDITOR, "Rich Text Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), null),
    CODE_EDITOR(EditType.CODE_EDITOR, "Code Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), "codeEditType = @CodeEditorType(language = \"sql\")"),
    MARKDOWN(EditType.MARKDOWN, "Markdown Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), null),
    COLOR(EditType.COLOR, "Color Picker", String.class.getSimpleName(), null),
    ICON(EditType.ICON, "Icon Picker", String.class.getSimpleName(), null),
    NUMBER(EditType.NUMBER, "Number Input", Integer.class.getSimpleName(), "numberType = @NumberType"),
    SLIDER(EditType.SLIDER, "Slider", Integer.class.getSimpleName(), "sliderType = @SliderType(max = 999)"),
    RATE(EditType.RATE, "Rating", Short.class.getSimpleName(), "rateType = @RateType(count = 10)"),
    DATE(EditType.DATE, "Date", Date.class.getSimpleName(), "dateType = @DateType"),
    DATE_TIME(EditType.DATE, "Date Time", Date.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.DATE_TIME)"),
    TIME(EditType.DATE, "Time", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.TIME)"),
    WEEK(EditType.DATE, "Week", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.WEEK)"),
    MONTH(EditType.DATE, "Month", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.MONTH)"),
    YEAR(EditType.DATE, "Year", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.YEAR)"),
    BOOLEAN(EditType.BOOLEAN, "Switch", Boolean.class.getSimpleName(), "boolType = @BoolType"),
    CHOICE(EditType.CHOICE, "Dropdown", String.class.getSimpleName(), "choiceType = @ChoiceType(vl = {@VL(value = \"xxx\", label = \"xxx\"), @VL(value = \"yyy\", label = \"yyy\")})"),
    MULTI_CHOICE(EditType.MULTI_CHOICE, "Multi Select", String.class.getSimpleName(), "multiChoiceType = @MultiChoiceType(vl = {@VL(value = \"xxx\", label = \"xxx\"), @VL(value = \"yyy\", label = \"yyy\")})"),
    TAGS(EditType.TAGS, "Tags Selector", String.class.getSimpleName(), "tagsType = @TagsType"),
    ATTACHMENT(EditType.ATTACHMENT, "Attachment", String.class.getSimpleName(), "attachmentType = @AttachmentType"),
    IMAGE(EditType.ATTACHMENT, "Image", String.class.getSimpleName(), "attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)"),
    AUTO_COMPLETE(EditType.AUTO_COMPLETE, "Auto Complete", String.class.getSimpleName(), "autoCompleteType = @AutoCompleteType(handler = AutoCompleteHandler.class)"),
    MAP(EditType.MAP, "Map", String.class.getSimpleName(), null),
    SIGNATURE(EditType.SIGNATURE, "Signature Pad", String.class.getSimpleName(), null),

    DIVIDE(EditType.DIVIDE, "Divider", String.class.getSimpleName(), null) {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@" + Transient.class.getSimpleName();
        }
    },

    GROUP(EditType.GROUP, "Field Group", String.class.getSimpleName(), "groupType = @GroupType(fields = {\"field1\", \"field2\"})") {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@" + Transient.class.getSimpleName();
        }
    },

    KEY_VALUE(EditType.KEY_VALUE, "Key-Value Pairs", null, null) {
        // a JSON column holding the map itself; a String + JSON column would be double-encoded by Hibernate
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@" + JdbcTypeCode.class.getSimpleName() + "(" + SqlTypes.class.getSimpleName() + ".JSON)";
        }

        @Override
        public String fieldType(GeneratorField field) {
            return "Map<String, String>";
        }

        @Override
        public String[] imports() {
            return new String[]{Map.class.getName(), JdbcTypeCode.class.getName(), SqlTypes.class.getName()};
        }
    },

    COMBINE(EditType.COMBINE, "One-to-One Add", null, null) {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@OneToOne(cascade = CascadeType.ALL)\n@JoinColumn" + joinColumn(field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return field.getLinkClass();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    REFERENCE_TREE(EditType.REFERENCE_TREE, "Tree Reference", null, "referenceTreeType = @ReferenceTreeType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@ManyToOne\n@JoinColumn" + joinColumn(field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return field.getLinkClass();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    REFERENCE_TABLE(EditType.REFERENCE_TABLE, "Table Reference", null, "referenceTableType = @ReferenceTableType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return REFERENCE_TREE.annotation(clazz, field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return REFERENCE_TREE.fieldType(field);
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    TAB_TABLE_REFER(EditType.TAB_TABLE_REFER, "One-to-Many Reference", null, null) {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return CHECKBOX.annotation(clazz, field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return CHECKBOX.fieldType(field);
        }

        @Override
        public String[] imports() {
            return CHECKBOX.imports();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    TAB_TABLE_ADD(EditType.TAB_TABLE_ADD, "One-to-Many Add", null, null) {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return "@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)\n@OrderBy\n@JoinColumn(name = \""
                    + Naming.humpToLine(clazz.getClassName()) + "_id\")";
        }

        @Override
        public String fieldType(GeneratorField field) {
            return CHECKBOX.fieldType(field);
        }

        @Override
        public String[] imports() {
            return CHECKBOX.imports();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    CHECKBOX(EditType.CHECKBOX, "Multi-select", null, "checkboxType = @CheckboxType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            String self = Naming.humpToLine(clazz.getClassName());
            String link = Naming.humpToLine(field.getLinkClass());
            return "@ManyToMany\n@JoinTable(name = \"" + self + "_" + link + "\",\n"
                    + "        joinColumns = @JoinColumn(name = \"" + self + "_id\", referencedColumnName = \"id\"),\n"
                    + "        inverseJoinColumns = @JoinColumn(name = \"" + link + "_id\", referencedColumnName = \"id\"))";
        }

        @Override
        public String fieldType(GeneratorField field) {
            return "Set<" + field.getLinkClass() + ">";
        }

        @Override
        public String[] imports() {
            return new String[]{Set.class.getName()};
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    TRANSFER(EditType.TRANSFER, "Transfer", null, "transferType = @TransferType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return CHECKBOX.annotation(clazz, field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return CHECKBOX.fieldType(field);
        }

        @Override
        public String[] imports() {
            return CHECKBOX.imports();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    TAB_TREE(EditType.TAB_TREE, "Multi-select Tree", null, null) {
        @Override
        public String annotation(GeneratorClass clazz, GeneratorField field) {
            return CHECKBOX.annotation(clazz, field);
        }

        @Override
        public String fieldType(GeneratorField field) {
            return CHECKBOX.fieldType(field);
        }

        @Override
        public String[] imports() {
            return CHECKBOX.imports();
        }

        @Override
        public boolean ref() {
            return true;
        }
    },

    HIDDEN(EditType.HIDDEN, "Hidden", String.class.getSimpleName(), null),
    EMPTY(EditType.EMPTY, "Empty", String.class.getSimpleName(), null);

    private static final String[] NO_IMPORTS = new String[0];

    //a varchar wider than this reads better in a textarea
    private static final int TEXTAREA_SIZE = 500;

    private final EditType mapping;

    private final String name;

    private final String type;

    private final String code;

    GeneratorType(EditType mapping, String name, String type, String code) {
        this.mapping = mapping;
        this.name = name;
        this.type = type;
        this.code = code;
    }

    @SuppressWarnings("unused")
    public String annotation(GeneratorClass clazz, GeneratorField field) {
        return null;
    }

    public String fieldType(GeneratorField field) {
        return this.type;
    }

    public String[] imports() {
        return NO_IMPORTS;
    }

    //whether the field points at another entity instead of holding a value
    public boolean ref() {
        return false;
    }

    //fields the user is likely to search on; text blobs and media are not among them
    public boolean searchable() {
        return switch (this) {
            case TEXTAREA, HTML_EDITOR, CODE_EDITOR, MARKDOWN, PASSWORD, KEY_VALUE, ATTACHMENT, IMAGE, SIGNATURE, MAP,
                 HIDDEN, EMPTY -> false;
            default -> !this.ref();
        };
    }

    protected static String joinColumn(GeneratorField field) {
        return null == field.getColumnName() || field.getColumnName().isEmpty()
                ? "" : "(name = \"" + field.getColumnName() + "\")";
    }

    /**
     * Guess the edit type of a jdbc column, name hints win over the raw type
     * because a varchar tells nothing about what it holds.
     */
    public static GeneratorType of(int jdbcType, String typeName, int size, String column) {
        return switch (jdbcType) {
            case Types.BIT, Types.BOOLEAN -> BOOLEAN;
            case Types.TINYINT -> size <= 1 ? BOOLEAN : NUMBER;
            case Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.DECIMAL, Types.NUMERIC, Types.REAL, Types.FLOAT,
                 Types.DOUBLE -> NUMBER;
            case Types.DATE -> DATE;
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> TIME;
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> DATE_TIME;
            case Types.CLOB, Types.NCLOB, Types.LONGVARCHAR, Types.LONGNVARCHAR -> TEXTAREA;
            default -> ofString(typeName, size, column);
        };
    }

    private static GeneratorType ofString(String typeName, int size, String column) {
        String type = null == typeName ? "" : typeName.toLowerCase(Locale.ROOT);
        if (type.contains("json")) return KEY_VALUE;
        String col = Naming.humpToLine(column);
        if (contains(col, "password", "passwd", "pwd", "secret")) return PASSWORD;
        if (contains(col, "avatar", "image", "img", "photo", "picture", "logo", "cover", "thumb")) return IMAGE;
        if (contains(col, "attachment", "annex", "file_url", "file_path")) return ATTACHMENT;
        if (contains(col, "icon")) return ICON;
        if (contains(col, "color", "colour")) return COLOR;
        return size >= TEXTAREA_SIZE || size <= 0 ? TEXTAREA : INPUT;
    }

    /**
     * Java type of a numeric column, null means the edit type default applies.
     */
    public static String javaType(int jdbcType, int scale) {
        return switch (jdbcType) {
            case Types.BIGINT -> Long.class.getSimpleName();
            case Types.DECIMAL, Types.NUMERIC ->
                    scale > 0 ? BigDecimal.class.getSimpleName() : Long.class.getSimpleName();
            case Types.REAL, Types.FLOAT -> Float.class.getSimpleName();
            case Types.DOUBLE -> Double.class.getSimpleName();
            default -> null;
        };
    }

    private static boolean contains(String column, String... keywords) {
        for (String keyword : keywords) {
            if (column.contains(keyword)) return true;
        }
        return false;
    }

}
