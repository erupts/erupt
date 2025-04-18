package xyz.erupt.generator.base;

import lombok.Getter;
import xyz.erupt.annotation.sub_field.EditType;

import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.Set;

@Getter
public enum GeneratorType {
    INPUT(EditType.INPUT, "文本输入", String.class.getSimpleName(), "inputType = @InputType"),
    PASSWORD(EditType.INPUT, "密码输入", String.class.getSimpleName(), "inputType = @InputType(type = \"password\")"),
    TEXTAREA(EditType.TEXTAREA, "多行文本框", String.class.getSimpleName(), null),
    HTML_EDITOR(EditType.HTML_EDITOR, "富文本编辑器", "@" + Lob.class.getSimpleName() + " " + String.class.getSimpleName(), "htmlEditorType = @HtmlEditorType(HtmlEditorType.Type.UEDITOR)"),
    CODE_EDITOR(EditType.CODE_EDITOR, "代码编辑器", HTML_EDITOR.type + String.class.getSimpleName(), "codeEditType = @CodeEditorType(language = \"sql\")"),
    COLOR(EditType.COLOR, "颜色选择", String.class.getSimpleName(), null),
    NUMBER(EditType.NUMBER, "数值框", Integer.class.getSimpleName(), "numberType = @NumberType"),
    SLIDER(EditType.SLIDER, "数字滑块", Integer.class.getSimpleName(), "sliderType = @SliderType(max = 999)"),
    RATE(EditType.RATE, "评分", Short.class.getSimpleName(), "rateType = @RateType(count = 10)"),
    DATE(EditType.DATE, "日期", Date.class.getSimpleName(), "dateType = @DateType"),
    DATE_TIME(EditType.DATE, "日期时间", Date.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.DATE_TIME)"),
    TIME(EditType.DATE, "时间", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.TIME)"),
    WEEK(EditType.DATE, "周", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.WEEK)"),
    MONTH(EditType.DATE, "月", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.MONTH)"),
    YEAR(EditType.DATE, "年", String.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.YEAR)"),
    BOOLEAN(EditType.BOOLEAN, "开关", Boolean.class.getSimpleName(), "boolType = @BoolType"),
    CHOICE(EditType.CHOICE, "下拉选择", String.class.getSimpleName(), "choiceType = @ChoiceType(vl = {@VL(value = \"xxx\", label = \"xxx\"), @VL(value = \"yyy\", label = \"yyy\")})"),
    TAGS(EditType.TAGS, "标签选择器", String.class.getSimpleName(), "tagsType = @TagsType"),
    ATTACHMENT(EditType.ATTACHMENT, "附件", String.class.getSimpleName(), "attachmentType = @AttachmentType"),
    IMAGE(EditType.ATTACHMENT, "图片", String.class.getSimpleName(), "attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)"),
    AUTO_COMPLETE(EditType.AUTO_COMPLETE, "自动补全", String.class.getSimpleName(), "autoCompleteType = @AutoCompleteType(handler = AutoCompleteHandler.class)"),
    MAP(EditType.MAP, "地图", String.class.getSimpleName(), null),
    DIVIDE(EditType.DIVIDE, "分割线", String.class.getSimpleName(), null) {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return "@" + Transient.class.getSimpleName();
        }
    },
    @Ref COMBINE(EditType.COMBINE, "一对一新增", null, null) {
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
    @Ref REFERENCE_TREE(EditType.REFERENCE_TREE, "树引用", null, "referenceTreeType = @ReferenceTreeType(id = \"id\", label = \"name\")") {
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
    @Ref REFERENCE_TABLE(EditType.REFERENCE_TABLE, "表格引用", null, "referenceTableType = @ReferenceTableType(id = \"id\", label = \"name\")") {
        @Override
        public String annotation(String thisErupt, String linkErupt) {
            return REFERENCE_TREE.annotation(thisErupt, linkErupt);
        }

        @Override
        public String fieldType(String thisErupt, String linkErupt) {
            return REFERENCE_TREE.fieldType(thisErupt, linkErupt);
        }
    },
    @Ref TAB_TABLE_REFER(EditType.TAB_TABLE_REFER, "一对多引用", null, null) {
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
    @Ref TAB_TABLE_ADD(EditType.TAB_TABLE_ADD, "一对多新增", null, null) {
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
    @Ref CHECKBOX(EditType.CHECKBOX, "多选", null, "checkboxType = @CheckboxType(id = \"id\", label = \"name\")") {
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
    @Ref TAB_TREE(EditType.TAB_TREE, "多选树", null, null) {
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
//    TPL(EditType.TPL, "自定义模板", String.class.getSimpleName(), "tplType = @Tpl(path = \"/xxx.ftl\")") {
//        @Override
//        public String annotation(String thisErupt, String linkErupt) {
//            return "@" + Transient.class.getSimpleName();
//        }
//    },
    HIDDEN(EditType.HIDDEN, "隐藏", String.class.getSimpleName(), null),
    EMPTY(EditType.EMPTY, "空", String.class.getSimpleName(), null);

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

    //驼峰转下划线
    public static String humpToLine(String str) {
        String hump = str.replaceAll("[A-Z]", "_$0").toLowerCase();
        if (hump.startsWith("_")) {
            hump = hump.substring(1);
        }
        return hump;
    }

}
