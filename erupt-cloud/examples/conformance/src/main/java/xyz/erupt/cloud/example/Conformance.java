package xyz.erupt.cloud.example;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.cloud.node.lite.MemoryRepository;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.lite.User;

import java.util.Date;
import java.util.List;

/** The conformance fixture (conformance/README.md), declared with the regular erupt annotations. */
public final class Conformance {

    // a Java drill without a code is addressed by its title's hash, as erupt-core's: the fixture's code is the title
    @Erupt(name = "Category", drills = @Drill(code = "Articles", title = "Articles", link = @Link(linkErupt = Article.class, joinColumn = "category")))
    public static class Category {
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
    public static class PublishForm {
        @EruptField(views = @View(title = "Note"), edit = @Edit(title = "Note", notNull = true))
        String note;
    }

    @Erupt(name = "Article", orderBy = "id desc", rowOperation = {
            @RowOperation(code = "publish", title = "Publish", icon = "fa fa-paper-plane", eruptClass = PublishForm.class,
                    ifExpr = "item.status !== 'published'", operationHandler = Publish.class),
            @RowOperation(code = "feature", title = "Feature", mode = RowOperation.Mode.SINGLE, callHint = "", operationHandler = Feature.class),
            @RowOperation(code = "rebuild", title = "Rebuild", mode = RowOperation.Mode.BUTTON, show = @ExprBool(exprHandler = RootOnly.class),
                    operationHandler = Rebuild.class)})
    public static class Article {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Title", sortable = true),
                edit = @Edit(title = "Title", notNull = true, search = @Search, inputType = @InputType(length = 64)))
        String title;
        @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", search = @Search, type = EditType.CHOICE,
                choiceType = @ChoiceType(vl = {@VL(value = "draft", label = "Draft"), @VL(value = "published", label = "Published")})))
        String status;
        @EruptField(views = @View(title = "Views", sortable = true), edit = @Edit(title = "Views"))
        Long views;
        @EruptField(views = @View(title = "Featured"), edit = @Edit(title = "Featured", search = @Search))
        Boolean featured;
        @EruptField(views = @View(title = "Publish Date"), edit = @Edit(title = "Publish Date", search = @Search, dateType = @DateType(type = DateType.Type.DATE)))
        Date publish_date;
        @EruptField(views = @View(title = "Review At"), edit = @Edit(title = "Review At", dateType = @DateType(type = DateType.Type.DATE_TIME)))
        Date review_at;
        @EruptField(edit = @Edit(title = "Body", type = EditType.TEXTAREA))
        String body;
        @EruptField(views = @View(title = "Category", column = "name"), edit = @Edit(title = "Category", search = @Search))
        Category category;
        @EruptField(views = @View(title = "Topic", column = "name"), edit = @Edit(title = "Topic", type = EditType.REFERENCE_TREE,
                referenceTreeType = @ReferenceTreeType(pid = "parent")))
        Category topic;
    }

    @Erupt(name = "Note", power = @Power(delete = false), dataProxy = NoteProxy.class)
    public static class Note {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Text"), edit = @Edit(title = "Text"))
        String text;
        @EruptField(edit = @Edit(title = "Ping", type = EditType.BUTTON, buttonType = @ButtonType(handler = Ping.class)))
        Object ping;
    }

    public static class Ping implements EruptButtonHandler<Note> {
        @Override
        public String click(Note note, String[] params) {
            return "pong:" + note.text;
        }
    }

    public static class NoteProxy implements DataProxy<Note> {
        @Override
        public void addBehavior(Note note) {
            note.text = "new";
        }

        @Override
        public void validate(Note note) {
            if ("forbidden".equals(note.text)) throw new EruptException("forbidden text");
        }
    }

    public static class Publish implements OperationHandler<Article, PublishForm> {
        @Override
        public String exec(List<Article> rows, PublishForm form, String[] param) {
            var repo = Node.current().repository(Article.class);
            for (Article a : rows) {
                a.status = "published";
                repo.update(a);
            }
            return null;
        }
    }

    public static class Feature implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            return "alert('featured')";
        }
    }

    public static class Rebuild implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            return null;
        }
    }

    /** Shows the operation only to the account {@code root}. */
    public static class RootOnly implements ExprBool.ExprHandler {
        @Override
        public boolean handler(boolean expr, String[] params) {
            User user = Node.currentUser();
            return user != null && "root".equals(user.account());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv("PORT"));
        new Node("conformance", System.getenv("ERUPT_ACCESS_TOKEN"), System.getenv("ERUPT_SERVER"))
                .address("http://127.0.0.1:" + port)
                .register(Category.class, new MemoryRepository<>(Category.of("Tech", null), Category.of("Python", 1L), Category.of("Life", null)))
                .register(Article.class, new MemoryRepository<>())
                .register(Note.class, new MemoryRepository<>())
                .serve("127.0.0.1", port);
    }
}
