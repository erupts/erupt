package xyz.erupt.cloud.example.showcase;

import org.sqlite.SQLiteDataSource;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.AutoCompleteHandler;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.DragSort;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Sort;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.CalloutType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.annotation.sub_field.sub_edit.GroupType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.KeyValueType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.annotation.sub_field.sub_edit.RateType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.SliderType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.annotation.sub_field.sub_edit.TextareaType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.annotation.vis.BoardView;
import xyz.erupt.annotation.vis.CalendarView;
import xyz.erupt.annotation.vis.CardView;
import xyz.erupt.annotation.vis.TimelineView;
import xyz.erupt.cloud.node.lite.JdbcRepository;
import xyz.erupt.cloud.node.lite.Node;
import xyz.erupt.cloud.node.lite.Query;
import xyz.erupt.cloud.node.lite.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * The showcase admin on erupt-cloud-node-lite: every capability of the node, as SHOWCASE.md in erupt-sdk describes it
 * for every node SDK (its conformance/showcase.py checks it). Create the node on the erupt server first, then:
 *
 * <pre>
 * ERUPT_NODE_NAME=showcase ERUPT_ACCESS_TOKEN=... ERUPT_SERVER=http://localhost:8080 java -jar target/erupt-showcase.jar
 * </pre>
 * <p>
 * PORT (9090) and ERUPT_DB (showcase.db, a SQLite file) are optional. Field names are the storage columns, so the
 * members are named like them. A form-only element (CALLOUT, DIVIDE, EMPTY, GROUP, BUTTON) is never stored; a List
 * or Map member (MULTI_CHOICE, KEY_VALUE, and CHECKBOX / TRANSFER / TAB_TREE of another erupt) holds a JSON value.
 * The code the node runs for a field is the erupt-annotation handlers, which the build names "invoke".
 */
public final class Showcase {

    // ---------------------------------------------------------------- reference data

    @Erupt(name = "Category", tree = @Tree(pid = "parent"), dragSort = @DragSort(field = "sort"),
            drills = @Drill(code = "Articles", title = "Articles", link = @Link(linkErupt = Article.class, joinColumn = "category")))
    public static class Category {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name", sortable = true), edit = @Edit(title = "Name", notNull = true, search = @Search))
        String name;
        @EruptField(views = @View(title = "Parent Id"), edit = @Edit(title = "Parent Id"))
        Long parent;
        @EruptField(views = @View(title = "Sort", sortable = true), edit = @Edit(title = "Sort"))
        Long sort;
    }

    @Erupt(name = "Author", drills = @Drill(code = "Articles", title = "Articles",
            link = @Link(linkErupt = Article.class, joinColumn = "author", linkCondition = "status = 'published'")))
    public static class Author {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search))
        String name;
        @EruptField(views = @View(title = "Email"), edit = @Edit(title = "Email", inputType = @InputType(regex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")))
        String email;
        @EruptField(views = @View(title = "Active"), edit = @Edit(title = "Active", notNull = true, search = @Search))
        Boolean active;
    }

    @Erupt(name = "Tag")
    public static class Tag {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search))
        String name;
        @EruptField(views = @View(title = "Color"), edit = @Edit(title = "Color", type = EditType.COLOR))
        String color;
    }

    /** The form of the Publish operation; not registered. */
    @Erupt(name = "Publish Options")
    public static class PublishForm {
        @EruptField(edit = @Edit(title = "Publish Date", notNull = true))
        LocalDate publish_date;
    }

    @Erupt(name = "Audit Log", orderBy = "id desc", power = @Power(add = false, edit = false, delete = false))
    public static class AuditLog {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Action"), edit = @Edit(title = "Action", search = @Search, type = EditType.CHOICE,
                choiceType = @ChoiceType(vl = {@VL(value = "add", label = "Add"), @VL(value = "update", label = "Update"),
                        @VL(value = "delete", label = "Delete")})))
        String action;
        @EruptField(views = @View(title = "Article"), edit = @Edit(title = "Article"))
        String article;
        @EruptField(views = @View(title = "By"), edit = @Edit(title = "By", search = @Search))
        String account;
        @EruptField(views = @View(title = "At", sortable = true), edit = @Edit(title = "At", dateType = @DateType(type = DateType.Type.DATE_TIME)))
        LocalDateTime at;
    }

    // ---------------------------------------------------------------- the article: every field type and behaviour

    @Erupt(name = "Article", orderBy = "id desc", power = @Power(importable = true), dataProxy = ArticleProxy.class,
            linkTree = @LinkTree(field = "category"),
            vis = {
                    @Vis(code = "Drafts", title = "Drafts", filter = @Filter("status = 'draft'"), orderBy = @Sort(field = "views",
                            direction = xyz.erupt.annotation.query.Direction.DESC)),
                    @Vis(code = "Board", title = "Board", type = Vis.Type.BOARD, boardView = @BoardView(groupField = "status", sumField = "views")),
                    @Vis(code = "Calendar", title = "Calendar", type = Vis.Type.CALENDAR, calendarView = @CalendarView(dateField = "publish_date")),
                    @Vis(code = "Cards", title = "Cards", type = Vis.Type.CARD, cardView = @CardView(coverField = "cover")),
                    @Vis(code = "Timeline", title = "Timeline", type = Vis.Type.TIMELINE, timelineView = @TimelineView(dateField = "review_at")),
            },
            rowOperation = {
                    @RowOperation(code = "publish", title = "Publish", icon = "fa fa-paper-plane", eruptClass = PublishForm.class,
                            ifExpr = "item.status !== 'published'", operationHandler = Publish.class),
                    @RowOperation(code = "feature", title = "Feature", mode = RowOperation.Mode.SINGLE, icon = "fa fa-star",
                            callHint = "Feature this article on the home page?", operationHandler = Feature.class),
                    @RowOperation(code = "archive", title = "Archive", mode = RowOperation.Mode.MULTI_ONLY, icon = "fa fa-archive",
                            callHint = "", operationHandler = Archive.class),
                    @RowOperation(code = "stats", title = "Stats", mode = RowOperation.Mode.BUTTON, icon = "fa fa-bar-chart",
                            show = @ExprBool(exprHandler = AdminOnly.class), operationHandler = Stats.class),
            })
    public static class Article {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Title", sortable = true), edit = @Edit(title = "Title", notNull = true, search = @Search,
                inputType = @InputType(length = 60), onchange = SummaryFromTitle.class))
        String title;
        @EruptField(edit = @Edit(title = "Summary", type = EditType.TEXTAREA, textareaType = @TextareaType(length = 200)))
        String summary;
        @EruptField(views = @View(title = "Author", column = "name"), edit = @Edit(title = "Author", search = @Search))
        Author author;
        @EruptField(views = @View(title = "Category", column = "name"), edit = @Edit(title = "Category", type = EditType.REFERENCE_TABLE, search = @Search))
        Category category;
        @EruptField(edit = @Edit(title = "Sub Category", type = EditType.REFERENCE_TABLE,
                referenceTableType = @ReferenceTableType(dependField = "category", dependColumn = "parent")))
        Category sub_category;
        @EruptField(edit = @Edit(title = "Topic", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parent")))
        Category topic;
        @EruptField(edit = @Edit(title = "Tags (checkbox)", type = EditType.CHECKBOX))
        List<Tag> tag_ids;
        @EruptField(edit = @Edit(title = "Reviewers", type = EditType.TRANSFER))
        List<Author> reviewers;
        @EruptField(edit = @Edit(title = "Sections", type = EditType.TAB_TREE))
        List<Category> sections;
        @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", search = @Search, type = EditType.CHOICE,
                choiceType = @ChoiceType(vl = {@VL(value = "draft", label = "Draft"), @VL(value = "review", label = "In Review"),
                        @VL(value = "published", label = "Published"), @VL(value = "archived", label = "Archived")})))
        String status;
        @EruptField(views = @View(title = "Level"), edit = @Edit(title = "Level", type = EditType.CHOICE,
                choiceType = @ChoiceType(fetchHandler = ArticleLevels.class)))
        String level;
        @EruptField(views = @View(title = "Labels"), edit = @Edit(title = "Labels", type = EditType.TAGS,
                tagsType = @TagsType(fetchHandler = Labels.class, allowExtension = true, joinSeparator = ",")))
        String labels;
        @EruptField(views = @View(title = "City"), edit = @Edit(title = "City", type = EditType.AUTO_COMPLETE,
                autoCompleteType = @AutoCompleteType(handler = Cities.class, triggerLength = 1)))
        String city;
        @EruptField(views = @View(title = "Cover"), edit = @Edit(title = "Cover", type = EditType.ATTACHMENT,
                attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE, maxLimit = 1)))
        String cover;
        @EruptField(views = @View(title = "Views", sortable = true), edit = @Edit(title = "Views", numberType = @NumberType(min = 0)))
        Long views;
        @EruptField(views = @View(title = "Price"), edit = @Edit(title = "Price", numberType = @NumberType(min = 0, precision = 2, prefix = "$")))
        Double price;
        @EruptField(views = @View(title = "Rating"), edit = @Edit(title = "Rating", type = EditType.RATE, rateType = @RateType(count = 5, allowHalf = true)))
        Double rating;
        @EruptField(views = @View(title = "Progress"), edit = @Edit(title = "Progress", type = EditType.SLIDER,
                sliderType = @SliderType(min = 0, max = 100, step = 5)))
        Long progress;
        @EruptField(views = @View(title = "Featured"), edit = @Edit(title = "Featured", search = @Search))
        Boolean featured;
        @EruptField(views = @View(title = "Theme Color"), edit = @Edit(title = "Theme Color", type = EditType.COLOR))
        String color;
        @EruptField(edit = @Edit(title = "Icon", type = EditType.ICON))
        String icon;
        @EruptField(views = @View(title = "Publish Date"), edit = @Edit(title = "Publish Date", search = @Search(operator = QueryExpression.RANGE)))
        LocalDate publish_date;
        @EruptField(views = @View(title = "Review At", sortable = true), edit = @Edit(title = "Review At"))
        LocalDateTime review_at;
        @EruptField(edit = @Edit(title = "Content", type = EditType.HTML_EDITOR))
        String content;
        @EruptField(edit = @Edit(title = "Notes", type = EditType.MARKDOWN))
        String notes;
        @EruptField(edit = @Edit(title = "Meta (JSON)", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json")))
        String meta;
        @EruptField(edit = @Edit(title = "Preview", type = EditType.BUTTON, buttonType = @ButtonType(handler = Preview.class)))
        String preview;
    }

    // ---------------------------------------------------------------- the node's own code for the article's fields

    /** title onchange: suggest a summary while there is none. */
    public static class SummaryFromTitle implements OnChange<Article> {
        @Override
        public Map<String, Object> populateForm(Article form, String[] params) {
            if (form.title == null || form.title.isEmpty() || form.summary != null && !form.summary.isEmpty()) return null;
            return Map.of("summary", "About " + form.title);
        }
    }

    /** level options: an expert level once the article is read a lot. */
    public static class ArticleLevels implements ChoiceFetchHandler<Article> {
        @Override
        public List<VLModel> fetch(String[] params) {
            return fetchFilter(null, params);
        }

        @Override
        public List<VLModel> fetchFilter(Article form, String[] params) {
            List<VLModel> options = new ArrayList<>(List.of(new VLModel("beginner", "Beginner"), new VLModel("advanced", "Advanced")));
            if (form != null && form.views != null && form.views >= 1000) options.add(new VLModel("expert", "Expert"));
            return options;
        }
    }

    public static class Labels implements TagsFetchHandler<Article> {
        @Override
        public List<String> fetchTags(Article form, String[] params) {
            return List.of("hot", "new", "classic");
        }
    }

    public static class Cities implements AutoCompleteHandler<Article> {
        private static final List<String> CITIES = List.of("Paris", "Tokyo", "Lisbon", "Austin");

        @Override
        public List<Object> completeHandler(Article form, String typed, String[] params) {
            String text = typed == null ? "" : typed.toLowerCase(Locale.ROOT);
            return CITIES.stream().filter(c -> c.toLowerCase(Locale.ROOT).contains(text)).map(c -> (Object) c).toList();
        }
    }

    public static class Preview implements EruptButtonHandler<Article> {
        @Override
        public String click(Article form, String[] params) {
            return "alert(" + jsString("Preview: " + (form.title == null ? "" : form.title)) + ")";
        }
    }

    /** Every hook of a data proxy, in the order SHOWCASE.md lists them. */
    public static class ArticleProxy implements DataProxy<Article> {
        private static final System.Logger LOG = System.getLogger("showcase");
        private final AtomicLong fetched = new AtomicLong();

        @Override
        public void searchCondition(Map<String, Object> condition) {
            condition.put("status", "published");
        }

        @Override
        public String beforeFetch(List<Condition> conditions) {
            return "status <> 'archived'";
        }

        @Override
        public void afterFetch(Collection<Map<String, Object>> rows) {
            LOG.log(System.Logger.Level.DEBUG, "rows fetched so far: " + fetched.addAndGet(rows.size()));
        }

        @Override
        public void addBehavior(Article article) {
            article.status = "draft";
            article.views = 0L;
            article.progress = 0L;
        }

        @Override
        public void validate(Article article) {
            if ("forbidden".equals(article.title)) throw new EruptException("This title is not allowed");
        }

        @Override
        public void beforeAdd(Article article) {
            stampReview(article);
        }

        @Override
        public void beforeUpdate(Article article) {
            stampReview(article);
        }

        @Override
        public void afterAdd(Article article) {
            audit("add", article);
        }

        @Override
        public void afterUpdate(Article article) {
            audit("update", article);
        }

        @Override
        public void beforeDelete(Article article) {
            if ("published".equals(article.status)) throw new EruptException("Unpublish it first");
        }

        @Override
        public void afterDelete(Article article) {
            audit("delete", article);
        }

        private static void stampReview(Article article) {
            if ("review".equals(article.status) && article.review_at == null) article.review_at = now();
        }

        private static void audit(String action, Article article) {
            AuditLog log = new AuditLog();
            log.action = action;
            log.article = article.title;
            log.account = Node.currentUser() == null ? null : Node.currentUser().account();
            log.at = now();
            Node.current().repository(AuditLog.class).add(log);
        }
    }

    public static class Publish implements OperationHandler<Article, PublishForm> {
        @Override
        public String exec(List<Article> rows, PublishForm form, String[] param) {
            for (Article a : rows) {
                a.status = "published";
                a.publish_date = form.publish_date;
                articles().update(a);
            }
            return null;
        }
    }

    public static class Feature implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            rows.get(0).featured = true;
            articles().update(rows.get(0));
            return null;
        }
    }

    public static class Archive implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            for (Article a : rows) {
                a.status = "archived";
                articles().update(a);
            }
            return null;
        }
    }

    public static class Stats implements OperationHandler<Article, Void> {
        @Override
        public String exec(List<Article> rows, Void form, String[] param) {
            // the articles the table shows: before_fetch leaves the archived ones out
            long total = articles().page(new Query(List.of(), List.of(), 0, 1, List.of("status <> 'archived'"))).total();
            return "alert('" + total + " articles')";
        }
    }

    /** The Stats operation is the admin's. */
    public static class AdminOnly implements ExprBool.ExprHandler {
        @Override
        public boolean handler(boolean expr, String[] params) {
            return Node.currentUser() != null && "admin".equals(Node.currentUser().account());
        }
    }

    private static Repository<Article> articles() {
        return Node.current().repository(Article.class);
    }

    // ---------------------------------------------------------------- layout and JSON-valued components

    @Erupt(name = "Component")
    public static class Component {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search))
        String name;
        @EruptField(edit = @Edit(title = "Guide", type = EditType.CALLOUT,
                calloutType = @CalloutType(value = "Layout and JSON-valued components.", style = CalloutType.Style.INFO)))
        String intro;
        @EruptField(edit = @Edit(title = "Basic", type = EditType.DIVIDE))
        String basic;
        @EruptField(edit = @Edit(title = "Code", type = EditType.HIDDEN))
        String code;
        @EruptField(views = @View(title = "Roles"), edit = @Edit(title = "Roles", search = @Search, type = EditType.MULTI_CHOICE,
                multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.CHECKBOX, vl = {@VL(value = "ADMIN", label = "Admin"),
                        @VL(value = "USER", label = "User"), @VL(value = "GUEST", label = "Guest")})))
        List<String> roles;
        @EruptField(views = @View(title = "Languages"), edit = @Edit(title = "Languages", type = EditType.MULTI_CHOICE,
                multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, vl = {@VL(value = "JAVA", label = "Java"),
                        @VL(value = "PYTHON", label = "Python"), @VL(value = "GO", label = "Go"), @VL(value = "RUST", label = "Rust")})))
        List<String> langs;
        @EruptField(views = @View(title = "Attributes"), edit = @Edit(title = "Attributes", type = EditType.KEY_VALUE,
                keyValueType = @KeyValueType(keyPlaceholder = "Key", valuePlaceholder = "Value")))
        Map<String, String> attrs;
        @EruptField(edit = @Edit(title = "", type = EditType.EMPTY))
        String spacer;
        @EruptField(edit = @Edit(title = "Extra", type = EditType.DIVIDE))
        String extra;
        @EruptField(views = @View(title = "Location"), edit = @Edit(title = "Location", type = EditType.MAP))
        String location;
        @EruptField(edit = @Edit(title = "Signature", type = EditType.SIGNATURE))
        String signature;
        @EruptField(edit = @Edit(title = "Place & Sign", type = EditType.GROUP, groupType = @GroupType(fields = {"location", "signature"})))
        String place;
    }

    // ---------------------------------------------------------------- buttons, form behaviour, table display, server calls

    /** The form of the Rename operation; not registered. */
    @Erupt(name = "Rename")
    public static class RenameForm {
        @EruptField(edit = @Edit(title = "New Name", notNull = true))
        String name;
    }

    @Erupt(name = "Buttons & Forms", power = @Power(export = false), layout = @Layout(pageSize = 20, tableLeftFixed = 1),
            rowOperation = {
                    @RowOperation(code = "rename", title = "Rename", mode = RowOperation.Mode.SINGLE, eruptClass = RenameForm.class,
                            tip = "Give the row a new name", operationHandler = Rename.class),
                    @RowOperation(code = "double", title = "Double", ifExpr = "item.amount > 0",
                            ifExprBehavior = RowOperation.IfExprBehavior.HIDE, callHint = "", operationHandler = DoubleAmount.class),
                    @RowOperation(code = "reset_amounts", title = "Reset Amounts", fold = true, callHint = "Reset the amounts?",
                            operationHandler = ResetAmounts.class),
                    @RowOperation(code = "node_info", title = "Node Info", mode = RowOperation.Mode.BUTTON, icon = "fa fa-info-circle",
                            callHint = "", operationHandler = NodeInfo.class),
            })
    public static class Playground {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, placeHolder = "a short name",
                desc = "Shown first in the table", search = @Search(operator = QueryExpression.LIKE, lockOperator = true),
                inputType = @InputType(prefix = @VL(value = "@", label = "@"), suffix = {@VL(value = ".dev", label = ".dev"), @VL(value = ".io", label = ".io")})))
        String name;
        @EruptField(views = @View(title = "Kind"), edit = @Edit(title = "Kind", search = @Search, type = EditType.CHOICE,
                choiceType = @ChoiceType(vl = {@VL(value = "simple", label = "Simple"), @VL(value = "advanced", label = "Advanced")})))
        String kind;
        @EruptField(views = @View(title = "Level"), edit = @Edit(title = "Level", type = EditType.CHOICE,
                choiceType = @ChoiceType(fetchHandler = PlaygroundLevels.class, dependField = "kind")))
        String level;
        @EruptField(edit = @Edit(title = "Advanced Note", type = EditType.TEXTAREA,
                dynamic = @Dynamic(dependField = "kind", condition = "value === 'advanced'", match = Dynamic.Ctrl.SHOW, noMatch = Dynamic.Ctrl.HIDE)))
        String note;
        @EruptField(views = @View(title = "Code"), edit = @Edit(title = "Code", readonly = @Readonly(add = false, edit = true)))
        String code;
        @EruptField(views = @View(title = "Amount", statistic = View.Statistic.SUM, className = "text-right", width = "120px"),
                edit = @Edit(title = "Amount", numberType = @NumberType(prefix = "$")))
        Double amount;
        @EruptField(views = @View(title = "Score", statistic = View.Statistic.AVG), edit = @Edit(title = "Score"))
        Double score;
        @EruptField(views = @View(title = "Skills"), edit = @Edit(title = "Skills", type = EditType.MULTI_CHOICE,
                multiChoiceType = @MultiChoiceType(fetchHandler = Skills.class)))
        List<String> skills;
        @EruptField(edit = @Edit(title = "Files", type = EditType.ATTACHMENT,
                attachmentType = @AttachmentType(type = AttachmentType.Type.BASE, maxLimit = 3, fileTypes = {"pdf", "txt", "md"})))
        String files;
        @EruptField(edit = @Edit(title = "Fill Sample", type = EditType.BUTTON,
                buttonType = @ButtonType(style = "primary", icon = "fa fa-magic", handler = FillSample.class)))
        String fill;
        @EruptField(edit = @Edit(title = "Clear", type = EditType.BUTTON,
                buttonType = @ButtonType(danger = true, confirm = "Clear the form?", handler = ClearForm.class)))
        String reset;
        @EruptField(edit = @Edit(title = "Who Am I", type = EditType.BUTTON,
                buttonType = @ButtonType(fullSpan = true, icon = "fa fa-user", handler = WhoAmI.class)))
        String whoami;
    }

    /** level options follow the kind (choiceType.dependField). */
    public static class PlaygroundLevels implements ChoiceFetchHandler<Playground> {
        @Override
        public List<VLModel> fetch(String[] params) {
            return fetchFilter(null, params);
        }

        @Override
        public List<VLModel> fetchFilter(Playground form, String[] params) {
            return form != null && "advanced".equals(form.kind)
                    ? List.of(new VLModel("high", "High"), new VLModel("expert", "Expert"))
                    : List.of(new VLModel("low", "Low"), new VLModel("mid", "Mid"));
        }
    }

    public static class Skills implements ChoiceFetchHandler<Playground> {
        @Override
        public List<VLModel> fetch(String[] params) {
            return List.of(new VLModel("java", "Java"), new VLModel("python", "Python"), new VLModel("go", "Go"));
        }
    }

    public static class FillSample implements EruptButtonHandler<Playground> {
        @Override
        public Map<String, Object> populateForm(Playground form, String[] params) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("name", "sample");
            values.put("kind", "advanced");
            values.put("amount", 42);
            values.put("score", 4);
            return values;
        }
    }

    public static class ClearForm implements EruptButtonHandler<Playground> {
        @Override
        public Map<String, Object> populateForm(Playground form, String[] params) {
            return Map.of("name", "", "amount", 0, "score", 0);
        }
    }

    public static class WhoAmI implements EruptButtonHandler<Playground> {
        @Override
        public String click(Playground form, String[] params) {
            return "alert(" + jsString(account()) + ")";
        }
    }

    public static class Rename implements OperationHandler<Playground, RenameForm> {
        @Override
        public String exec(List<Playground> rows, RenameForm form, String[] param) {
            rows.get(0).name = form.name;
            playgrounds().update(rows.get(0));
            return null;
        }
    }

    public static class DoubleAmount implements OperationHandler<Playground, Void> {
        @Override
        public String exec(List<Playground> rows, Void form, String[] param) {
            for (Playground p : rows) {
                p.amount = p.amount == null ? 0 : p.amount * 2;
                playgrounds().update(p);
            }
            return null;
        }
    }

    public static class ResetAmounts implements OperationHandler<Playground, Void> {
        @Override
        public String exec(List<Playground> rows, Void form, String[] param) {
            for (Playground p : rows) {
                p.amount = 0d;
                playgrounds().update(p);
            }
            return null;
        }
    }

    /** Calls the server back: the acting user's info and the node's config (0 when a call fails). */
    public static class NodeInfo implements OperationHandler<Playground, Void> {
        @Override
        public String exec(List<Playground> rows, Void form, String[] param) {
            int roles = 0, config = 0;
            try {
                if (Node.userInfo().get("roles") instanceof List<?> list) roles = list.size();
            } catch (Exception ignored) {
                // the server could not answer: 0
            }
            try {
                config = Node.config().length();
            } catch (Exception ignored) {
                // the server could not answer: 0
            }
            return "alert(" + jsString(account() + ": " + roles + " roles, config " + config + " chars") + ")";
        }
    }

    private static Repository<Playground> playgrounds() {
        return Node.current().repository(Playground.class);
    }

    // ---------------------------------------------------------------- helpers

    private static String account() {
        return Node.currentUser() == null || Node.currentUser().account() == null ? "" : Node.currentUser().account();
    }

    /** {@code text} as a JavaScript string literal. */
    static String jsString(String text) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '<' -> sb.append("\\u003c"); // never closes a script element
                default -> {
                    if (c < 0x20 || c == ' ' || c == ' ') sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    // the local wall clock to the second
    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // ---------------------------------------------------------------- storage and seed data

    private static final String[] TABLES = {
            "CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, parent INTEGER, sort INTEGER)",
            "CREATE TABLE IF NOT EXISTS author (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, active INTEGER)",
            "CREATE TABLE IF NOT EXISTS tag (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, color TEXT)",
            "CREATE TABLE IF NOT EXISTS audit_log (id INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT, article TEXT, account TEXT, at TEXT)",
            "CREATE TABLE IF NOT EXISTS article (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, summary TEXT, author INTEGER, "
            + "category INTEGER, sub_category INTEGER, topic INTEGER, tag_ids TEXT, reviewers TEXT, sections TEXT, status TEXT, "
            + "level TEXT, labels TEXT, city TEXT, cover TEXT, views INTEGER, price REAL, rating REAL, progress INTEGER, "
            + "featured INTEGER, color TEXT, icon TEXT, publish_date TEXT, review_at TEXT, content TEXT, notes TEXT, meta TEXT)",
            "CREATE TABLE IF NOT EXISTS component (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, code TEXT, roles TEXT, "
            + "langs TEXT, attrs TEXT, location TEXT, signature TEXT)",
            "CREATE TABLE IF NOT EXISTS playground (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, kind TEXT, level TEXT, "
            + "note TEXT, code TEXT, amount REAL, score REAL, skills TEXT, files TEXT)",
    };

    /** Adds {@code records} while the table is empty, so a restart keeps what was edited. */
    @SafeVarargs
    private static <T> void seed(Repository<T> repo, Supplier<T>... records) {
        if (repo.page(new Query(List.of(), List.of(), 0, 1)).total() > 0) return;
        for (Supplier<T> r : records) repo.add(r.get());
    }

    private static <T> T ref(Supplier<T> type, java.util.function.Consumer<T> key) {
        T t = type.get();
        key.accept(t);
        return t;
    }

    private static Category category(long id) {
        return ref(Category::new, c -> c.id = id);
    }

    private static Author author(long id) {
        return ref(Author::new, a -> a.id = id);
    }

    private static Tag tag(long id) {
        return ref(Tag::new, t -> t.id = id);
    }

    private static Category newCategory(String name, Long parent, long sort) {
        Category c = new Category();
        c.name = name;
        c.parent = parent;
        c.sort = sort;
        return c;
    }

    private static Author newAuthor(String name, String email, boolean active) {
        Author a = new Author();
        a.name = name;
        a.email = email;
        a.active = active;
        return a;
    }

    private static Tag newTag(String name, String color) {
        Tag t = new Tag();
        t.name = name;
        t.color = color;
        return t;
    }

    private static Article hello() {
        Article a = new Article();
        a.title = "Hello Erupt";
        a.summary = "A first look at erupt nodes";
        a.author = author(1);
        a.category = category(2);
        a.topic = category(2);
        a.tag_ids = List.of(tag(1), tag(2));
        a.reviewers = List.of(author(2));
        a.sections = List.of(category(1), category(2));
        a.status = "published";
        a.level = "beginner";
        a.labels = "hot,new";
        a.city = "Paris";
        a.views = 120L;
        a.price = 9.9;
        a.rating = 4.5;
        a.progress = 100L;
        a.featured = true;
        a.color = "#1677ff";
        a.icon = "fa fa-book";
        a.publish_date = LocalDate.of(2026, 10, 1);
        a.review_at = LocalDateTime.of(2026, 10, 2, 10, 0);
        a.content = "<p>Hello</p>";
        a.notes = "# Hello";
        a.meta = "{\"a\":1}";
        return a;
    }

    private static Article goTips() {
        Article a = new Article();
        a.title = "Go Tips";
        a.author = author(2);
        a.category = category(3);
        a.topic = category(3);
        a.tag_ids = List.of(tag(2));
        a.status = "review";
        a.level = "advanced";
        a.labels = "classic";
        a.views = 30L;
        a.price = 0d;
        a.rating = 3d;
        a.progress = 60L;
        a.featured = false;
        return a;
    }

    private static Article weekendTrip() {
        Article a = new Article();
        a.title = "Weekend Trip";
        a.author = author(3);
        a.category = category(5);
        a.topic = category(5);
        a.status = "draft";
        a.views = 0L;
        a.rating = 0d;
        a.progress = 10L;
        a.featured = false;
        return a;
    }

    private static Component component(String name, String code, List<String> roles, List<String> langs, Map<String, String> attrs) {
        Component c = new Component();
        c.name = name;
        c.code = code;
        c.roles = roles;
        c.langs = langs;
        c.attrs = attrs;
        return c;
    }

    private static Playground playground(String name, String kind, String level, String note, String code, double amount,
                                         double score, List<String> skills) {
        Playground p = new Playground();
        p.name = name;
        p.kind = kind;
        p.level = level;
        p.note = note;
        p.code = code;
        p.amount = amount;
        p.score = score;
        p.skills = skills;
        return p;
    }

    /**
     * The showcase node, its models registered over the SQLite file and seeded, not yet serving: a framework launcher
     * (showcase-solon, -quarkus, -micronaut, -vertx, -ktor, -servlet) serves this very node.
     */
    public static Node node() throws Exception {
        SQLiteDataSource sqlite = new SQLiteDataSource();
        sqlite.setUrl("jdbc:sqlite:" + env("ERUPT_DB", "showcase.db"));
        DataSource db = sqlite;
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            for (String ddl : TABLES) st.execute(ddl);
        }
        JdbcRepository<Category> categories = new JdbcRepository<>(db, "category");
        JdbcRepository<Author> authors = new JdbcRepository<>(db, "author");
        JdbcRepository<Tag> tags = new JdbcRepository<>(db, "tag");
        JdbcRepository<Article> articles = new JdbcRepository<>(db, "article");
        JdbcRepository<Component> components = new JdbcRepository<>(db, "component");
        JdbcRepository<Playground> playgrounds = new JdbcRepository<>(db, "playground");
        Node node = new Node(env("ERUPT_NODE_NAME", "showcase"), System.getenv("ERUPT_ACCESS_TOKEN"), env("ERUPT_SERVER", "http://localhost:8080"))
                .register(Category.class, categories)
                .register(Author.class, authors)
                .register(Tag.class, tags)
                .register(AuditLog.class, new JdbcRepository<>(db, "audit_log"))
                .register(Article.class, articles)
                .register(Component.class, components)
                .register(Playground.class, playgrounds);
        seed(categories, () -> newCategory("Tech", null, 1), () -> newCategory("Python", 1L, 2), () -> newCategory("Go", 1L, 3),
                () -> newCategory("Life", null, 4), () -> newCategory("Travel", 4L, 5));
        seed(authors, () -> newAuthor("Ann", "ann@example.com", true), () -> newAuthor("Bob", "bob@example.com", true),
                () -> newAuthor("Cy", "cy@example.com", false));
        seed(tags, () -> newTag("intro", "#1677ff"), () -> newTag("guide", "#52c41a"), () -> newTag("news", "#fa8c16"));
        seed(articles, Showcase::hello, Showcase::goTips, Showcase::weekendTrip);
        seed(components, () -> component("Sample A", "C-001", List.of("ADMIN", "USER"), List.of("JAVA", "GO"), Map.of("env", "prod", "tier", "1")),
                () -> component("Sample B", "C-002", List.of("GUEST"), List.of("PYTHON"), Map.of()));
        seed(playgrounds, () -> playground("alpha", "simple", "low", null, "A-1", 10, 3, List.of("java")),
                () -> playground("beta", "advanced", "expert", "deep dive", "B-1", 0, 5, List.of("python", "go")),
                () -> playground("gamma", "simple", "mid", null, "G-1", 25, 4, List.of()));
        return node;
    }

    /** The port to listen on: {@code PORT}, 9090 without it. */
    public static int port() {
        return Integer.parseInt(env("PORT", "9090"));
    }

    /** On the JDK's HTTP server. */
    public static void main(String[] args) throws Exception {
        node().serve("0.0.0.0", port());
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isEmpty() ? fallback : value;
    }

    private Showcase() {
    }
}
