package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ComponentPool {
    
    // Since Adventure Components are immutable, we cache frequently used simple components
    private static final ConcurrentHashMap<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger CACHE_SIZE = new AtomicInteger(0);
    private static final int MAX_CACHE_SIZE = 1000;
    
    public static Component getOrCreateComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.text("");
        }
        
        // Check cache first
        Component cached = COMPONENT_CACHE.get(text);
        if (cached != null) {
            return cached;
        }
        
        // Create new component if cache is not full
        if (CACHE_SIZE.get() < MAX_CACHE_SIZE) {
            Component newComponent = Component.text(text);
            Component existing = COMPONENT_CACHE.putIfAbsent(text, newComponent);
            if (existing == null) {
                CACHE_SIZE.incrementAndGet();
                return newComponent;
            } else {
                return existing;
            }
        }
        
        // Cache is full, create without caching
        return Component.text(text);
    }
    
    public static Component getEmptyComponent() {
        return getOrCreateComponent("");
    }
    
    public static void clearCache() {
        COMPONENT_CACHE.clear();
        CACHE_SIZE.set(0);
    }
    
    public static int getCacheSize() {
        return CACHE_SIZE.get();
    }
    
    public static int getMaxCacheSize() {
        return MAX_CACHE_SIZE;
    }
    
}
