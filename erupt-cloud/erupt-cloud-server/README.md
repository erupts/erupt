# erupt-cloud-server

Control plane for Erupt distributed deployments.

Manages a fleet of `erupt-cloud-node` workers: registers nodes, distributes configuration, and proxies API and UI requests to the appropriate node. Provides an admin UI for node health monitoring and management. Built on top of `erupt-admin` with Ant Design UI.
