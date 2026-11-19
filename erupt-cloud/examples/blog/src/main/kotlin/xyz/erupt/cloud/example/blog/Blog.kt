// A blog admin served from Kotlin, stored in an H2 file. Create node "blog" on the erupt server first, then:
//
//   ERUPT_ACCESS_TOKEN=... ERUPT_SERVER=http://localhost:8080 java -jar target/erupt-blog.jar
package xyz.erupt.cloud.example.blog

import org.h2.jdbcx.JdbcDataSource
import xyz.erupt.annotation.Erupt
import xyz.erupt.annotation.EruptField
import xyz.erupt.annotation.`fun`.DataProxy
import xyz.erupt.annotation.`fun`.OperationHandler
import xyz.erupt.annotation.sub_erupt.RowOperation
import xyz.erupt.annotation.sub_field.Edit
import xyz.erupt.annotation.sub_field.EditType
import xyz.erupt.annotation.sub_field.View
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType
import xyz.erupt.annotation.sub_field.sub_edit.Search
import xyz.erupt.cloud.node.lite.JdbcRepository
import xyz.erupt.cloud.node.lite.Node
import java.time.LocalDate
import java.time.LocalDateTime

@Erupt(name = "Category")
class Category {
    @EruptField
    var id: Long? = null

    @EruptField(views = [View(title = "Name")], edit = Edit(title = "Name", notNull = true, search = Search()))
    var name: String? = null

    @EruptField(views = [View(title = "Parent Id")], edit = Edit(title = "Parent Id"))
    var parent: Long? = null
}

@Erupt(name = "Publish Options")
class PublishForm {
    @EruptField(edit = Edit(title = "Publish Date", notNull = true))
    var publishDate: LocalDate? = null
}

/** An enum member is a CHOICE: its constants are the options. */
enum class Status { DRAFT, PUBLISHED }

@Erupt(
    name = "Article", orderBy = "id desc", dataProxy = [ArticleProxy::class],
    rowOperation = [
        RowOperation(code = "publish", title = "Publish", icon = "fa fa-paper-plane", eruptClass = PublishForm::class,
            ifExpr = "item.status !== 'PUBLISHED'", operationHandler = Publish::class),
        RowOperation(code = "feature", title = "Feature", mode = RowOperation.Mode.SINGLE, icon = "fa fa-star",
            callHint = "Feature this article on the home page?", operationHandler = Feature::class),
    ],
)
class Article {
    @EruptField
    var id: Long? = null

    @EruptField(views = [View(title = "Title", sortable = true)], edit = Edit(title = "Title", notNull = true, search = Search()))
    var title: String? = null

    // picked from a tree the server builds out of the categories' parent ids
    @EruptField(
        views = [View(title = "Category", column = "name")],
        edit = Edit(title = "Category", search = Search(), type = EditType.REFERENCE_TREE, referenceTreeType = ReferenceTreeType(pid = "parent")),
    )
    var category: Category? = null

    @EruptField(edit = Edit(title = "Status", search = Search()), views = [View(title = "Status")])
    var status: Status? = null

    @EruptField(views = [View(title = "Views", sortable = true)], edit = Edit(title = "Views"))
    var views: Long? = null

    @EruptField(views = [View(title = "Featured")], edit = Edit(title = "Featured", search = Search()))
    var featured: Boolean? = null

    @EruptField(views = [View(title = "Publish Date")], edit = Edit(title = "Publish Date", search = Search()))
    var publishDate: LocalDate? = null

    @EruptField(views = [View(title = "Updated At", sortable = true)], edit = Edit(title = "Updated At", show = false))
    var updatedAt: LocalDateTime? = null

    @EruptField(edit = Edit(title = "Body", type = EditType.TEXTAREA))
    var body: String? = null
}

/** Lifecycle hooks, exactly as in a Spring erupt application. */
class ArticleProxy : DataProxy<Article> {
    override fun beforeAdd(article: Article) {
        article.status = article.status ?: Status.DRAFT
        article.updatedAt = LocalDateTime.now()
    }

    override fun beforeUpdate(article: Article) {
        article.updatedAt = LocalDateTime.now()
    }
}

class Publish : OperationHandler<Article, PublishForm> {
    override fun exec(rows: List<Article>, form: PublishForm, param: Array<String>): String? {
        val repo = Node.current().repository(Article::class.java)
        rows.forEach {
            it.status = Status.PUBLISHED
            it.publishDate = form.publishDate
            repo.update(it)
        }
        return null
    }
}

class Feature : OperationHandler<Article, Void> {
    override fun exec(rows: List<Article>, form: Void?, param: Array<String>): String? {
        rows.single().featured = true
        Node.current().repository(Article::class.java).update(rows.single())
        return null
    }
}

fun main() {
    val dataSource = JdbcDataSource().apply { setURL("jdbc:h2:./blog") }
    dataSource.connection.use { conn ->
        conn.createStatement().use {
            it.execute("""CREATE TABLE IF NOT EXISTS "category" ("id" BIGINT AUTO_INCREMENT PRIMARY KEY, "name" VARCHAR(255), "parent" BIGINT)""")
            it.execute(
                """CREATE TABLE IF NOT EXISTS "article" ("id" BIGINT AUTO_INCREMENT PRIMARY KEY, "title" VARCHAR(255),
                  "category" BIGINT, "status" VARCHAR(16), "views" BIGINT, "featured" BOOLEAN, "publishDate" DATE,
                  "updatedAt" TIMESTAMP(3), "body" CLOB)""",
            )
        }
    }
    Node(System.getenv("ERUPT_NODE_NAME") ?: "blog", System.getenv("ERUPT_ACCESS_TOKEN"), System.getenv("ERUPT_SERVER") ?: "http://localhost:8080")
        .register(Category::class.java, JdbcRepository(dataSource, "category"))
        .register(Article::class.java, JdbcRepository(dataSource, "article"))
        .serve("0.0.0.0", System.getenv("PORT")?.toInt() ?: 9090)
}
