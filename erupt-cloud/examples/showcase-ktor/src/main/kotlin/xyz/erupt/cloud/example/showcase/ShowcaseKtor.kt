package xyz.erupt.cloud.example.showcase

import io.ktor.server.cio.CIO
import xyz.erupt.cloud.node.ktor.embeddedEruptNode

/** The showcase on a Ktor server of its own (erupt-cloud-node-ktor); calls run on Dispatchers.IO. */
fun main() {
    embeddedEruptNode(CIO, Showcase.node(), port = Showcase.port()).start(wait = true)
}
