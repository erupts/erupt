# erupt-tpl

Template engine abstraction layer for Erupt.

Provides a unified `EruptTemplate` interface backed by pluggable engines: FreeMarker, Thymeleaf, Apache Velocity, Beetl, and Enjoy. Other modules (erupt-print, erupt-generator, erupt-cloud-server) use this abstraction to render dynamic HTML, emails, and code templates without being coupled to a specific engine.
