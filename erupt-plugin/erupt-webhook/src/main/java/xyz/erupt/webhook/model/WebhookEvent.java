package xyz.erupt.webhook.model;

/**
 * The pipeline events a webhook can subscribe to; the names travel as the payload's {@code event}.
 */
public enum WebhookEvent {
    ADD, UPDATE, DELETE
}
