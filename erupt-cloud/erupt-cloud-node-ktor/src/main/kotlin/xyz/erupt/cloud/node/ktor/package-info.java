/**
 * Serves an erupt-cloud-node-lite node from a Ktor 3 server: {@code routing { eruptNode(node) }} mounts it next to
 * the application's routes, {@code embeddedEruptNode(CIO, node, port)} gives it a server of its own. Written in
 * Kotlin; from Java the functions are static members of {@code EruptNodeKtor}.
 *
 * @author YuePeng
 * date 2026/10/16
 */
package xyz.erupt.cloud.node.ktor;
