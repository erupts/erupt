# erupt-job

Scheduled job management for Erupt, powered by [Quartz Scheduler](http://www.quartz-scheduler.org/).

Provides a web UI to create, edit, pause, and trigger cron jobs without restarting the application. Supports email notifications on job failure and execution history logging. Jobs are defined as Spring beans and registered dynamically through the admin interface.
