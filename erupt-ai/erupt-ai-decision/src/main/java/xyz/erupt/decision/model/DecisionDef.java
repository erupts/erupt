package xyz.erupt.decision.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.decision.handler.DecisionTestHandler;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A decision declared in the admin rather than in code: its questions live in the database and
 * callers reach them by code, which is what lets a workflow, a script or another service ask
 * for a judgement without a deployment.
 *
 * @author YuePeng
 */
@Erupt(
        name = "Decision", dataProxy = DecisionDefDataProxy.class,
        rowOperation = @RowOperation(code = "test", title = "Test Run", icon = "fa fa-bolt",
                mode = RowOperation.Mode.SINGLE, eruptClass = DecisionTest.class,
                operationHandler = DecisionTestHandler.class)
)
@Table(name = "e_ai_decision_def", uniqueConstraints = @UniqueConstraint(name = "uk_decision_def_code", columnNames = "code"))
@Getter
@Setter
@Entity
@EruptI18n
public class DecisionDef extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Decision Code", sortable = true),
            edit = @Edit(title = "Decision Code", notNull = true, search = @Search(operator = QueryExpression.LIKE),
                    desc = "The name the API and Decisions.run() address this decision by")
    )
    private String code;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "decision_model_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Decision Model", column = "name"),
            edit = @Edit(title = "Decision Model", type = EditType.REFERENCE_TABLE,
                    desc = "Leave blank to use the default decision model")
    )
    private DecisionModel decisionModel;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", notNull = true, search = @Search, type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "Active", falseText = "Locked"))
    )
    private Boolean enable = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", type = ViewType.HTML),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "decision_def_id")
    @OrderBy("sort")
    @EruptField(
            edit = @Edit(title = "Questions", type = EditType.TAB_TABLE_ADD,
                    desc = "Asked together in one call: the state is read once and every question is answered against it")
    )
    private Set<DecisionQuestion> questions = new LinkedHashSet<>();

}
