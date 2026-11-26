package xyz.erupt.ai.constants;

public enum SseEvent {

    CALL,
    TOKEN,
    DONE,
    // The chat was renamed (a title generated from its first message); data is the new title
    TITLE

}
