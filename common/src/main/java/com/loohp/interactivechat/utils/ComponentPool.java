package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ComponentPool {
    
    private static final ConcurrentLinkedQueue<Component> COMPONENT_POOL = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger POOL_SIZE = new AtomicInteger(0);
    private static final int MAX_POOL_SIZE = 1000;
    
    public static Component borrowComponent() {
        Component component = COMPONENT_POOL.poll();
        if (component == null) {
            return Component.text("");
        }
        POOL_SIZE.decrementAndGet();
        return component;
    }
    
    public static void returnComponent(Component component) {
        if (component != null && POOL_SIZE.get() < MAX_POOL_SIZE) {
            if (component instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) component;
                if (textComponent.children().isEmpty() && textComponent.style().isEmpty()) {
                    COMPONENT_POOL.offer(Component.text(""));
                    POOL_SIZE.incrementAndGet();
                }
            }
        }
    }
    
    public static void clearPool() {
        COMPONENT_POOL.clear();
        POOL_SIZE.set(0);
    }
    
    public static int getPoolSize() {
        return POOL_SIZE.get();
    }
    
}
