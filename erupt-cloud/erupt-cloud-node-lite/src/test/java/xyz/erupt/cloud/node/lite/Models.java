package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Sort;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Test models, declared the way an erupt application declares them. */
final class Models {

    static final List<String> CALLS = new ArrayList<>();

    @Erupt(name = "Category")
    static class Category {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", search = @Search))
        String name;
        @EruptField(views = @View(title = "Parent"), edit = @Edit(title = "Parent"))
        Long parent;

        static Category of(String name, Long parent) {
            Category c = new Category();
            c.name = name;
            c.parent = parent;
            return c;
        }
    }

    @Erupt(name = "Publish Options")
    static class PublishForm {
        @EruptField(edit = @Edit(title = "Note", notNull = true))
        String note;
    }

    enum Level { LOW, HIGH }

    @Erupt(name = "Article", orderBy = "id desc", dataProxy = Stamp.class, rowOperation = {
            @RowOperation(code = "publish", title = "Publish", eruptClass = PublishForm.class, operationHandler = Publish.class),
            @RowOperation(code = "feature", title = "Feature", mode = RowOperation.Mode.SINGLE, callHint = "", operationHandler = Feature.class),
            @RowOperation(code = "rebuild", title = "Rebuild", mode = RowOperation.Mode.BUTTON, show = @ExprBool(exprHandler = RootOnly.class),
                    operationHandler = Rebuild.class)}, vis = {
            @Vis(code = "q", title = "Question", filter = @Filter("\"title\" = 'a?'")),
            @Vis(title = "Reversed", orderBy = @Sort(field = "title", direction = Direction.DESC))}, linkTree = @LinkTree(field = "topic"))
    static class Article {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Title", sortable = true), edit = @Edit(title = "Title", notNull = true, search = @Search))
        String title;
        @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", search = @Search, type = EditType.CHOICE,
                choiceType = @ChoiceType(vl = {@VL(value = "draft", label = "Draft"), @VL(value = "published", label = "Published")})))
        String status;
        @EruptField(views = @View(title = "Level"), edit = @Edit(title = "Level", search = @Search))
        Level level;
        @EruptField(views = @View(title = "Views", sortable = true), edit = @Edit(title = "Views"))
        Integer views;
        @EruptField(views = @View(title = "Featured"), edit = @Edit(title = "Featured", search = @Search))
        Boolean featured;
        @EruptField(views = @View(title = "Day"), edit = @Edit(title = "Day", search = @Search, dateType = @DateType(type = DateType.Type.DATE)))
        LocalDate day;
        @EruptField(views = @View(title = "At"), edit = @Edit(title = "At", dateType = @DateType(type = DateType.Type.DATE_TIME)))
        LocalDateTime at;
        @EruptField(views = @View(title = "Stamp"), edit = @Edit(title = "Stamp", show = false))
        String stamp;
        @EruptField(views = @View(title = "Category", column = "name"), edit = @Edit(title = "Category", search = @Search))
        Category category;
        @EruptField(views = @View(title = "Topic", column = "name"), edit = @Edit(title = "Topic", type = EditType.REFERENCE_TREE,
                referenceTreeType = @ReferenceTreeType(pid = "parent")))
        Category topic;
    }

    @Erupt(name = "Note", power = @Power(delete = false))
    static class Note {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Text"), edit = @Edit(title = "Text"))
        String text;
    }

    static class Stamp implements DataProxy<Article> {
        @Override
        public void beforeAdd(Article a) {
            a.stamp = "added";
        }

        @Override
        public void beforeUpdate(Article a) {
            a.stamp = "updated";
        }
    }

    static class Publish implements OperationHandler<Article, PublishForm> {
        @Override
        public String exec(List<Article> rows, PublishForm form, String[] param) {
            CALLS.add("publish " + rows.size() + " " + form.note);
            rows.forEach(a -> {
                a.status = "published";
                Node.current().repository(Article.class).update(a);
            });
            return null;
        }
    }

    static class Feature implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            return "alert('featured')";
        }
    }

    static class Rebuild implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            CALLS.add("rebuild " + rows.size());
            return null;
        }
    }

    static class RootOnly implements ExprBool.ExprHandler {
        @Override
        public boolean handler(boolean expr, String[] params) {
            return Node.currentUser() != null && "root".equals(Node.currentUser().account());
        }
    }

    private Models() {
    }
}
