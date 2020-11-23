package xyz.erupt.generator.base;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.sub_field.EditType;

import java.util.Date;

@Getter
public enum GeneratorType {
    INPUT(EditType.INPUT, "文本输入", GeneratorType.STRING, "inputType = @InputType"),
    PASSWORD(EditType.INPUT, "密码输入", GeneratorType.STRING, "inputType = @InputType(type = \"password\")"),
    COLOR(EditType.INPUT, "颜色选择", GeneratorType.STRING, "inputType = @InputType(type = \"color\")"),
    NUMBER(EditType.NUMBER, "数值框", Integer.class.getSimpleName(), "numberType = @NumberType"),
    SLIDER(EditType.SLIDER, "数字滑块", Integer.class.getSimpleName(), "sliderType = @SliderType(max = 999)"),
    DATE(EditType.DATE, "日期", Date.class.getSimpleName(), "dateType = @DateType"),
    DATE_TIME(EditType.DATE, "日期时间", Date.class.getSimpleName(), "dateType = @DateType(type = DateType.Type.DATE_TIME)"),
    WEEK(EditType.DATE, "周", GeneratorType.STRING, "dateType = @DateType(type = DateType.Type.MONTH)"),
    MONTH(EditType.DATE, "月", GeneratorType.STRING, "dateType = @DateType(type = DateType.Type.WEEK)"),
    YEAR(EditType.DATE, "年", GeneratorType.STRING, "dateType = @DateType(type = DateType.Type.YEAR)"),
    BOOLEAN(EditType.BOOLEAN, "开关", Boolean.class.getSimpleName(), "boolType = @BoolType"),
    CHOICE(EditType.CHOICE, "下拉选择", GeneratorType.STRING, "choiceType = @ChoiceType(vl = {@VL(value = \"xxx\", label = \"xxx\"), @VL(value = \"yyy\", label = \"yyy\")})"),
    TAGS(EditType.TAGS, "标签选择器", GeneratorType.STRING, "tagsType = @TagsType"),
    ATTACHMENT(EditType.ATTACHMENT, "附件", GeneratorType.STRING, "attachmentType = @AttachmentType"),
    IMAGE(EditType.ATTACHMENT, "图片", GeneratorType.STRING, "attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)"),
    AUTO_COMPLETE(EditType.ATTACHMENT, "自动补全", GeneratorType.STRING, "autoCompleteType = @AutoCompleteType(handler = AutoCompleteHandler.class)"),
    HTML_EDITOR(EditType.HTML_EDITOR, "富文本编辑器", GeneratorType.STRING, "htmlEditorType = @HtmlEditorType(HtmlEditorType.Type.UEDITOR)"),
    TEXTAREA(EditType.TEXTAREA, "多行文本框", GeneratorType.STRING, null),
    CODE_EDITOR(EditType.CODE_EDITOR, "代码编辑器", GeneratorType.STRING, "codeEditType = @CodeEditorType(language = \"sql\")"),
    @Ref REFERENCE_TREE(EditType.REFERENCE_TREE, "树引用", "@ManyToOne " + GeneratorType.REF, "referenceTreeType = @ReferenceTreeType(id = \"id\", label = \"name\")"),
    @Ref REFERENCE_TABLE(EditType.REFERENCE_TABLE, "表格引用", "@ManyToOne " + GeneratorType.REF, "referenceTableType = @ReferenceTableType(id = \"id\", label = \"name\")"),
    @Ref CHECKBOX(EditType.CHECKBOX, "多选", GeneratorType.ManyToMany, "checkboxType = @CheckboxType(id = \"id\", label = \"name\")"),
    @Ref TAB_TREE(EditType.TAB_TREE, "多选树", GeneratorType.ManyToMany, null),
    @Ref TAB_TABLE_REFER(EditType.TAB_TABLE_REFER, "一对多引用", GeneratorType.OneToMany, null),
    @Ref TAB_TABLE_ADD(EditType.TAB_TABLE_ADD, "一对多新增", GeneratorType.OneToMany, null),
    MAP(EditType.MAP, "地图", GeneratorType.STRING, null),
    TPL(EditType.TPL, "自定义模板", GeneratorType.Transient + GeneratorType.STRING, "tplType = @Tpl(path = \"/xxx.ftl\")"),
    DIVIDE(EditType.DIVIDE, "分割线", GeneratorType.Transient + GeneratorType.STRING, null),
    HIDDEN(EditType.HIDDEN, "隐藏", GeneratorType.STRING, null),
    EMPTY(EditType.EMPTY, "空", GeneratorType.STRING, null);

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

    public static final String REF = "#ref";
    private static final String STRING = "String";
    private static final String Transient = "@Transient ";
    private static final String SET = "Set<" + REF + ">";

    private static final String OneToMany = "@OneToMany(cascade = CascadeType.ALL)\n" +
            "                @JoinColumn(name = \"xxx_id\") " + GeneratorType.SET;

    private static final String ManyToMany = "@ManyToMany @JoinTable(name = \"xxx_table\",\n" +
            "            joinColumns = @JoinColumn(name = \"aaa_id\", referencedColumnName = \"id\"),\n" +
            "            inverseJoinColumns = @JoinColumn(name = \"bbb_id\", referencedColumnName = \"id\")) " + SET;

    public static String replaceLinkClass(String str, String linkClass) {
        if (StringUtils.isNotBlank(linkClass)) {
            return str.replace(REF, linkClass).replace("<", "&lt;").replace(">", "&gt;");
        } else {
            return str;
        }
    }

    //生成linkClass展示条件js脚本
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        try {
            for (GeneratorType type : GeneratorType.values()) {
                if (null != GeneratorType.class.getDeclaredField(type.name()).getAnnotation(Ref.class)) {
                    sb.append("value=='" + type.name() + "'||");
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
    }

}
