# erupt-cloud-node

Worker node for Erupt distributed deployments.

Registers with an `erupt-cloud-server` on startup, receives proxied requests, and executes them locally. Enables a single unified admin UI to manage data across multiple independently deployed Spring Boot applications. Uses Spring AOP for transparent request interception.
