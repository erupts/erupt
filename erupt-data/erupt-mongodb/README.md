# erupt-mongodb

MongoDB data layer for Erupt, built on Spring Data MongoDB.

Connects `@Erupt`-annotated document models to MongoDB. Implements the same Erupt data SPI as `erupt-jpa`, so the core engine's CRUD, pagination, and search features work identically for document collections.

Use this module as a drop-in replacement for `erupt-jpa` when your data lives in MongoDB.
