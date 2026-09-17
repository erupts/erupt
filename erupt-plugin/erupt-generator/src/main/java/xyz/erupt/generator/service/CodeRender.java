package xyz.erupt.generator.service;

import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.base.SuperModel;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.generator.model.GeneratorField;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Renders a {@link GeneratorClass} into a ready to paste erupt entity.
 *
 * @author YuePeng
 * date 2026-09-17
 */
public class CodeRender {

    private static final String INDENT = "    ";

    private static final String[] BASE_IMPORTS = {
            "jakarta.persistence.*",
            "lombok.Getter",
            "lombok.Setter",
            "xyz.erupt.annotation.*",
            "xyz.erupt.annotation.sub_erupt.*",
            "xyz.erupt.annotation.sub_field.*",
            "xyz.erupt.annotation.sub_field.sub_edit.*"
    };

    //java types that need an import when a field resolves to them
    private static final Map<String, String> TYPE_IMPORTS = Map.of(
            Date.class.getSimpleName(), Date.class.getName(),
            BigDecimal.class.getSimpleName(), BigDecimal.class.getName(),
            Set.class.getSimpleName(), Set.class.getName(),
            Map.class.getSimpleName(), Map.class.getName()
    );

    //default jpa column length, rendering it would only be noise
    private static final int DEFAULT_LENGTH = 255;

    public static String render(GeneratorClass clazz) {
        List<GeneratorField> fields = fields(clazz);
        Set<String> imports = new TreeSet<>(List.of(BASE_IMPORTS));
        Optional.ofNullable(clazz.getSuperClass().getPackageName()).ifPresent(imports::add);
        StringBuilder body = new StringBuilder();
        for (GeneratorField field : fields) {
            body.append(renderField(clazz, field, imports));
        }
        StringBuilder code = new StringBuilder();
        if (null != clazz.getPackageName() && !clazz.getPackageName().isEmpty()) {
            code.append("package ").append(clazz.getPackageName()).append(";\n\n");
        }
        for (String pkg : imports) {
            code.append("import ").append(pkg).append(";\n");
        }
        code.append("\n");
        //a name already taken would break the application the moment this class is added
        if (null != EruptCoreService.getErupt(clazz.getClassName())) {
            code.append("//FIXME an erupt model named ").append(clazz.getClassName()).append(" already exists\n");
        }
        if (null != clazz.getRemark() && !clazz.getRemark().isEmpty()) {
            code.append("/**\n * ").append(clazz.getRemark().replace("*/", "*").replace("\n", "\n * ")).append("\n */\n");
        }
        code.append("@Erupt(name = \"").append(quote(clazz.getName())).append("\"");
        primaryKey(clazz, fields).ifPresent(it -> code.append(", primaryKeyCol = \"").append(it).append("\""));
        code.append(")\n");
        code.append("@Table(name = \"").append(clazz.getTableName()).append("\")\n");
        code.append("@Entity\n@Getter\n@Setter\n");
        code.append("public class ").append(clazz.getClassName());
        if (SuperModel.NONE != clazz.getSuperClass()) {
            code.append(" extends ").append(clazz.getSuperClass().getClassName());
        }
        code.append(" {\n\n").append(body).append("}\n");
        return code.toString();
    }

    private static String renderField(GeneratorClass clazz, GeneratorField field, Set<String> imports) {
        GeneratorType type = field.getType();
        String fieldType = fieldType(field);
        Collections.addAll(imports, type.imports());
        Optional.ofNullable(TYPE_IMPORTS.get(baseType(fieldType))).ifPresent(imports::add);
        StringBuilder sb = new StringBuilder();
        sb.append(INDENT).append("@EruptField(\n");
        sb.append(INDENT).append(INDENT).append("views = @View(title = \"").append(quote(field.getShowName())).append("\"");
        if (Boolean.TRUE.equals(field.getSortable())) sb.append(", sortable = true");
        if (!Boolean.TRUE.equals(field.getIsShow())) sb.append(", show = false");
        sb.append("),\n");
        sb.append(INDENT).append(INDENT).append("edit = @Edit(title = \"").append(quote(field.getShowName())).append("\"");
        sb.append(", type = EditType.").append(type.getMapping().name());
        if (Boolean.TRUE.equals(field.getQuery())) sb.append(", search = @Search");
        if (!Boolean.TRUE.equals(field.getIsShow())) sb.append(", show = false");
        if (Boolean.TRUE.equals(field.getNotNull())) sb.append(", notNull = true");
        String typeCode = null == field.getTypeCode() || field.getTypeCode().isEmpty() ? type.getCode() : field.getTypeCode();
        if (null != typeCode) sb.append(",\n").append(INDENT).append(INDENT).append(INDENT).append(typeCode);
        sb.append(")\n").append(INDENT).append(")\n");
        if (SuperModel.NONE == clazz.getSuperClass() && Boolean.TRUE.equals(field.getPrimaryKey())) {
            sb.append(INDENT).append("@Id\n");
            if (Boolean.TRUE.equals(field.getAutoIncrement())) {
                sb.append(INDENT).append("@GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            }
        }
        appendLines(sb, type.annotation(clazz, field));
        appendLines(sb, column(field, fieldType));
        sb.append(INDENT).append("private ").append(fieldType).append(" ").append(field.getFieldName()).append(";\n\n");
        return sb.toString();
    }

    //@Column is only rendered when it carries information jpa cannot derive
    private static String column(GeneratorField field, String fieldType) {
        if (field.getType().ref()) return null;
        List<String> attrs = new ArrayList<>();
        if (null != field.getColumnName() && !field.getColumnName().isEmpty()) {
            attrs.add("name = \"" + field.getColumnName() + "\"");
        }
        if (null != field.getLength() && field.getLength() > 0 && field.getLength() != DEFAULT_LENGTH
                && fieldType.endsWith(String.class.getSimpleName()) && !fieldType.startsWith("@Lob")) {
            attrs.add("length = " + field.getLength());
        }
        if (Boolean.TRUE.equals(field.getUnique())) attrs.add("unique = true");
        return attrs.isEmpty() ? null : "@Column(" + String.join(", ", attrs) + ")";
    }

    private static String fieldType(GeneratorField field) {
        return null == field.getJavaType() || field.getJavaType().isEmpty()
                ? field.getType().fieldType(field) : field.getJavaType();
    }

    //Set<Dept> -> Set, @Lob String -> String
    private static String baseType(String fieldType) {
        String type = fieldType.contains("<") ? fieldType.substring(0, fieldType.indexOf('<')) : fieldType;
        return type.substring(type.lastIndexOf(' ') + 1);
    }

    private static Optional<String> primaryKey(GeneratorClass clazz, List<GeneratorField> fields) {
        if (SuperModel.NONE != clazz.getSuperClass()) return Optional.empty();
        return fields.stream().filter(it -> Boolean.TRUE.equals(it.getPrimaryKey()))
                .map(GeneratorField::getFieldName).filter(it -> !"id".equals(it)).findFirst();
    }

    private static List<GeneratorField> fields(GeneratorClass clazz) {
        List<GeneratorField> fields = new ArrayList<>(Optional.ofNullable(clazz.getFields()).orElse(Set.of()));
        fields.sort(Comparator.comparing(it -> null == it.getSort() ? 0 : it.getSort()));
        return fields;
    }

    private static void appendLines(StringBuilder sb, String annotation) {
        if (null == annotation || annotation.isEmpty()) return;
        for (String line : annotation.split("\n")) {
            sb.append(INDENT).append(line).append("\n");
        }
    }

    //a comment may carry quotes or line breaks, a java literal may not
    private static String quote(String text) {
        return null == text ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    private CodeRender() {
    }

}
