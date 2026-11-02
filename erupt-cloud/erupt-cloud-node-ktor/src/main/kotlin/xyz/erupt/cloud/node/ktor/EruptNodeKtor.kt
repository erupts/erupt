@file:JvmName("EruptNodeKtor")

package xyz.erupt.cloud.node.ktor

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.queryString
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.PathSegmentOptionalParameterRouteSelector
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.PathSegmentTailcardRouteSelector
import io.ktor.server.routing.PathSegmentWildcardRouteSelector
import io.ktor.server.routing.RootRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.RoutingNode
import io.ktor.server.routing.application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.erupt.cloud.common.consts.CloudRestApiConst
import xyz.erupt.cloud.node.lite.Node

/**
 * Serves an erupt node from a Ktor server, next to the application's own routes.
 *
 * ```kotlin
 * embeddedEruptNode(CIO, node, 9090).start(wait = true)   // a server of its own
 *
 * routing {                                              // or on the application's routing,
 *     route("/admin") { eruptNode(node) }                // under any constant path
 * }
 * ```
 *
 * The node connects once the server listens (on its port and mount path, unless `node.address(...)` is set) and
 * deregisters when the application stops.
 *
 * @author YuePeng
 * date 2026/10/16
 */
fun Route.eruptNode(node: Node): Route {
    val mount = mountPath(this)
    route(Node.ERUPT_API) { route("{...}") { handle { serve(node, mount) } } }
    route(CloudRestApiConst.NODE_HEALTH) { handle { serve(node, mount) } }
    val app = application
    app.monitor.subscribe(ApplicationStarted) {
        // the connectors are bound after the start event: wait for the port actually listened on
        app.launch(Dispatchers.IO) { node.connect(app.engine.resolvedConnectors().first().port, mount) }
    }
    app.monitor.subscribe(ApplicationStopped) { node.close() }
    return this
}

/** A server of the node's own on [port] (0 for any) with the given engine, e.g. `CIO` or `Netty`; start it to serve. */
fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> embeddedEruptNode(
    factory: ApplicationEngineFactory<E, C>, node: Node, port: Int = 0, host: String = "0.0.0.0",
): EmbeddedServer<E, C> = embeddedServer(factory, port = port, host = host) { routing { eruptNode(node) } }

private suspend fun RoutingContext.serve(node: Node, mount: String) {
    val req = call.request
    // a body-less request (a GET carrying an h2c upgrade, say) is not read: the engine would wait for one
    val hasBody = req.headers[HttpHeaders.ContentLength] != null || req.headers[HttpHeaders.TransferEncoding] != null
    val body = if (hasBody) call.receive<ByteArray>() else ByteArray(0)
    val target = Node.Request.target(mount, req.path(), req.queryString())
    // repositories block
    val resp = withContext(Dispatchers.IO) { node.handle(Node.Request(req.httpMethod.value, target, { req.headers[it] }, body)) }
    call.respondBytes(resp.body(), ContentType.parse(resp.contentType()), HttpStatusCode.fromValue(resp.status()))
}

/** The constant path a route sits below, the application's root path included: what the server's address ends with. */
private fun mountPath(route: Route): String {
    val segments = ArrayDeque<String>()
    var r = route as? RoutingNode
    while (r != null) {
        when (val selector = r.selector) {
            is PathSegmentConstantRouteSelector -> segments.addFirst(selector.value)
            is RootRouteSelector -> selector.toString().trim('/').takeIf { it.isNotEmpty() }?.let { segments.addFirst(it) }
            is PathSegmentParameterRouteSelector, is PathSegmentOptionalParameterRouteSelector,
            is PathSegmentWildcardRouteSelector, is PathSegmentTailcardRouteSelector ->
                throw IllegalArgumentException("mount the erupt node under a constant path, not $selector")
            else -> {} // methods, headers and the like select no path
        }
        r = r.parent
    }
    return segments.joinToString("") { "/$it" }
}
