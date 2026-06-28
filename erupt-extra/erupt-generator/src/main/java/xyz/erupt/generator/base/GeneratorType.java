package xyz.erupt.generator.base;

import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import lombok.Getter;
import xyz.erupt.annotation.sub_field.EditType;

import java.util.Date;
import java.util.Set;

@Getter
public enum GeneratorType {
    INPUT(EditType.INPUT, "Text Input", String.class.getSimpleName(), "inputType = @InputType"),
    PASSWORD(EditType.PASSWORD, "Password Input", String.class.getSimpleName(), null),
    TEXTAREA(EditType.TEXTAREA, "Textarea", String.class.getSimpleName(), null),
    HTML_EDITOR(EditType.HTML_EDITOR, "Rich Text Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), null),
    CODE_EDITOR(EditType.CODE_EDITOR, "Code Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), "codeEditType = @CodeEditorType(language = \"sql\")"),
    MARKDOWN(EditType.MARKDOWN, "Markdown Editor", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), null),
    COLOR(EditType.COLOR, "Color Picker", String.class.getSimpleName(), null),
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
        public String annotation(String thisErupt, String linkErupt) {
            return "@" + Transient.class.getSimpleName();
        }
    },
    GROUP(EditType.GROUP, "Field Group", String.class.getSimpleName(), "groupType = @GroupType(fields = {\"field1\", \"field2\"})") {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@" + Transient.class.getSimpleName();
        }
    },
    @Ref COMBINE(EditType.COMBINE, "One-to-One Add", null, null) {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@OneToOne(cascade = CascadeType.ALL)\n" +
                    "        @JoinColumn";
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return linkErupt;
        }
    },
    @Ref REFERENCE_TREE(EditType.REFERENCE_TREE, "Tree Reference", null, "referenceTreeType = @ReferenceTreeType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@ManyToOne\n" +
                    "        @JoinColumn";
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return linkErupt;
        }
    },
    @Ref REFERENCE_TABLE(EditType.REFERENCE_TABLE, "Table Reference", null, "referenceTableType = @ReferenceTableType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return REFERENCE_TREE.annotation(thisErupt, linkErupt);
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return REFERENCE_TREE.fieldType(thisErupt, linkErupt);
        }
    },
    @Ref TAB_TABLE_REFER(EditType.TAB_TABLE_REFER, "One-to-Many Reference", null, null) {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return CHECKBOX.annotation(thisErupt, linkErupt);
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return CHECKBOX.fieldType(thisErupt, linkErupt);
        }

        @Override
        public String importPackages() {
            return CHECKBOX.importPackages();
        }
    },
    @Ref TAB_TABLE_ADD(EditType.TAB_TABLE_ADD, "One-to-Many Add", null, null) {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)\n" +
                    "        @OrderBy\n" +
                    "        @JoinColumn(name = \"" + humpToLine(thisErupt) + "_id\") ";
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return CHECKBOX.fieldType(thisErupt, linkErupt);
        }

        @Override
        public String importPackages() {
            return CHECKBOX.importPackages();
        }
    },
    @Ref CHECKBOX(EditType.CHECKBOX, "Multi-select", null, "checkboxType = @CheckboxType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@ManyToMany \n" +
                    "        @JoinTable(name = \"" + humpToLine(thisErupt) + "_" + humpToLine(linkErupt) + "\",\n" +
                    "            joinColumns = @JoinColumn(name = \"" + humpToLine(thisErupt) + "_id\", referencedColumnName = \"id\"),\n" +
                    "            inverseJoinColumns = @JoinColumn(name = \"" + humpToLine(linkErupt) + "_id\", referencedColumnName = \"id\")) ";
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return ("Set<" + linkErupt + ">")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }

        @Override
        public String importPackages() {
            return "import " + Set.class.getName() + ";";
        }
    },
    @Ref TAB_TREE(EditType.TAB_TREE, "Multi-select Tree", null, null) {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return CHECKBOX.annotation(thisErupt, linkErupt);
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return CHECKBOX.fieldType(thisErupt, linkErupt);
        }

        @Override
        public String importPackages() {
            return CHECKBOX.importPackages();
        }
    },
    //    TPL(EditType.TPL, "Custom Template", String.class.getSimpleName(), "tplType = @Tpl(path = \"/xxx.ftl\")") {
//        @Override
//        public String annotation(String thisErupt, String linkErupt) {
//            return "@" + Transient.class.getSimpleName();
//        }
//    },
    HIDDEN(EditType.HIDDEN, "Hidden", String.class.getSimpleName(), null),
    EMPTY(EditType.EMPTY, "Empty", String.class.getSimpleName(), null);

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

    public String annotation(String thisErupt, String linkErupt) {
        return null;
    }

    public String fieldType(String thisErupt, String linkErupt) {
        return this.getType();
    }

    public String importPackages() {
        return null;
    }

    //Convert camelCase to underscore_case
    public static String humpToLine(String str) {
        String hump = str.replaceAll("[A-Z]", "_$0").toLowerCase();
        if (hump.startsWith("_")) {
            hump = hump.substring(1);
        }
        return hump;
    }

}
