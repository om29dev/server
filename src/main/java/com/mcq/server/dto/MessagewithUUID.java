package com.mcq.server.dto;

import java.util.UUID;

public class MessagewithUUID {
    private String message;
    private UUID uuid;

    public MessagewithUUID(String message) {
        this.message = message;
    }

    public MessagewithUUID(String message, UUID uuid) {
        this.message = message;
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public UUID getUuid() {
        return uuid;
    }

}
