package com.loohp.interactivechat.objectholders;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ProcessSenderResult {

    private final Component component;
    private final UUID sender;

    public ProcessSenderResult(Component component, UUID sender) {
        this.component = component;
        this.sender = sender;
    }

    public Component getComponent() {
        return component;
    }

    public UUID getSender() {
        return sender;
    }
}
