package xyz.erupt.cloud.node.ktor

import com.sun.net.httpserver.HttpServer
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.erupt.annotation.Erupt
import xyz.erupt.annotation.EruptField
import xyz.erupt.annotation.sub_field.Edit
import xyz.erupt.annotation.sub_field.View
import xyz.erupt.cloud.common.http.CloudSignature
import xyz.erupt.cloud.node.lite.MemoryRepository
import xyz.erupt.cloud.node.lite.Node
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class EruptNodeKtorTest {

    @Erupt(name = "Item")
    class Item {
        @EruptField
        var id: Long? = null

        @EruptField(views = [View(title = "Name")], edit = Edit(title = "Name"))
        var name: String? = null
    }

    private val registered = CompletableFuture<String>()
    // a default client offers an h2c upgrade on a GET; the server calls nodes over HTTP/1.1
    private val http = HttpClient.newHttpClient()
    private val server11 = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    private lateinit var eruptServer: HttpServer
    private lateinit var node: Node
    private var server: EmbeddedServer<*, *>? = null

    @BeforeEach
    fun setUp() {
        eruptServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        eruptServer.createContext("/erupt-cloud-api/register-node") { ex ->
            registered.complete(String(ex.requestBody.readAllBytes()))
            ex.sendResponseHeaders(200, -1)
            ex.close()
        }
        eruptServer.start()
        node = Node("ktor-node", TOKEN, "http://127.0.0.1:" + eruptServer.address.port)
            .register(Item::class.java, MemoryRepository())
    }

    @AfterEach
    fun tearDown() {
        server?.stop(0, 0)
        node.close()
        eruptServer.stop(0)
    }

    @Test
    fun serve() {
        val port = start(embeddedEruptNode(CIO, node, host = "127.0.0.1"))
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":$port\""), "registers the port it listens on")
        assertEquals("ktor-node", get(port, "/node-health"))
        assertTrue(signedGet(port, "", "/erupt-api/build/Item?_=a%2Fb").contains("\"Item\""))
    }

    @Test
    fun mountBesideApplicationRoutes() {
        val port = start(embeddedServer(CIO, port = 0, host = "127.0.0.1") {
            routing {
                get("/hello") { call.respondText("app") }
                route("/admin") { eruptNode(node) }
            }
        })
        assertTrue(registered.get(10, TimeUnit.SECONDS).contains(":$port/admin\""), "registers its mount path")
        assertEquals("app", get(port, "/hello"))
        assertEquals("ktor-node", get(port, "/admin/node-health"))
        // the server signs the path below the node's address, not the mount point
        assertTrue(signedGet(port, "/admin", "/erupt-api/build/Item").contains("\"Item\""))
    }

    @Test
    fun postBody() {
        val port = start(embeddedEruptNode(CIO, node, host = "127.0.0.1"))
        signed(port, "", "POST", "/erupt-api/data/modify/Item", """{"name":"ktor"}""")
        assertTrue(signed(port, "", "GET", "/erupt-api/data/Item/1", null).contains("\"ktor\""))
    }

    private fun start(server: EmbeddedServer<*, *>): Int {
        this.server = server.start(wait = false)
        return runBlocking { server.engine.resolvedConnectors().first().port }
    }

    private fun get(port: Int, path: String): String =
        http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path")).timeout(TIMEOUT).build(), HttpResponse.BodyHandlers.ofString()).body()

    private fun signedGet(port: Int, mount: String, target: String): String = signed(port, mount, "GET", target, null)

    private fun signed(port: Int, mount: String, method: String, target: String, body: String?): String {
        val now = System.currentTimeMillis() / 1000
        val bytes = body?.toByteArray() ?: ByteArray(0)
        // the power callback is skipped, as the server does when it has checked the user's power itself
        val enforced = CloudSignature.Enforced.header(setOf(CloudSignature.Enforced.POWER))
        val req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$mount$target")).timeout(TIMEOUT)
            .method(method, if (body == null) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofByteArray(bytes))
            .header("Content-Type", "application/json")
            .header(CloudSignature.HEADER_TIMESTAMP, now.toString())
            .header(CloudSignature.HEADER_ENFORCED, enforced)
            .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(TOKEN, method, target, now, enforced, null, bytes))
            .header("erupt", "Item").build()
        val resp = server11.send(req, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, resp.statusCode(), resp.body())
        return resp.body()
    }

    companion object {
        const val TOKEN = "node-secret"
        val TIMEOUT: Duration = Duration.ofSeconds(10)
    }
}
