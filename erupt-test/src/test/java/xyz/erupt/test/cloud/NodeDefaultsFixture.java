package xyz.erupt.test.cloud;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.util.Date;
import java.util.Set;

/**
 * One field per edit type a non-Java node SDK supports. {@link NodeDefaultsTest} serializes its build
 * model into schema/erupt-node-defaults.json, the template every SDK merges its declarations onto.
 * Each field name is the template key; {@code id} is the hidden primary key. Its single row operation is
 * the template of every operation.
 */
@Getter
@Setter
@Erupt(name = "NodeDefaultsFixture", authVerify = false,
        rowOperation = @RowOperation(code = "-", title = "-", eruptClass = NodeDefaultsRef.class))
public class NodeDefaultsFixture {

    @EruptField
    private Long id;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.INPUT, search = @Search))
    private String INPUT;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.TEXTAREA, search = @Search))
    private String TEXTAREA;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.NUMBER, search = @Search))
    private Long NUMBER;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.BOOLEAN, search = @Search))
    private Boolean BOOLEAN;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.DATE, search = @Search,
            dateType = @DateType(type = DateType.Type.DATE)))
    private Date DATE;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.DATE, search = @Search,
            dateType = @DateType(type = DateType.Type.DATE_TIME)))
    private Date DATE_TIME;

    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.CHOICE, search = @Search,
            choiceType = @ChoiceType(vl = @VL(value = "-", label = "-"))))
    private String CHOICE;

    @EruptField(views = @View(title = "-", column = "name"), edit = @Edit(title = "-", type = EditType.REFERENCE_TABLE, search = @Search))
    private NodeDefaultsRef REFERENCE_TABLE;

    @EruptField(views = @View(title = "-", column = "name"), edit = @Edit(title = "-", type = EditType.REFERENCE_TREE, search = @Search))
    private NodeDefaultsRef REFERENCE_TREE;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.MULTI_CHOICE, search = @Search,
            choiceType = @ChoiceType(vl = @VL(value = "-", label = "-"))))
    private String MULTI_CHOICE;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.TAGS, search = @Search))
    private String TAGS;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.AUTO_COMPLETE, search = @Search))
    private String AUTO_COMPLETE;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.ATTACHMENT))
    private String ATTACHMENT;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.HTML_EDITOR))
    private String HTML_EDITOR;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.MARKDOWN))
    private String MARKDOWN;
    @EruptField(views = @View(title = "-"), edit = @Edit(title = "-", type = EditType.CODE_EDITOR))
    private String CODE_EDITOR;
    @EruptField(edit = @Edit(title = "-", type = EditType.BUTTON))
    private String BUTTON;
    @EruptField(views = @View(title = "-", column = "name"), edit = @Edit(title = "-", type = EditType.CHECKBOX))
    private Set<NodeDefaultsRef> CHECKBOX;
    @EruptField(views = @View(title = "-", column = "name"), edit = @Edit(title = "-", type = EditType.TAB_TREE))
    private Set<NodeDefaultsRef> TAB_TREE;

}
