package com.loohp.interactivechat.proxy.objectholders;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ProxyHandlePacketTypes {

    public static final ProxyHandlePacketTypes ALL = new ProxyHandlePacketTypes(EnumSet.allOf(ProxyPacketType.class));

    public static ProxyHandlePacketTypes fromStringList(List<String> types) {
        Set<ProxyPacketType> set = EnumSet.noneOf(ProxyPacketType.class);
        for (String type : types) {
            set.add(ProxyPacketType.valueOf(type.toUpperCase()));
        }
        return new ProxyHandlePacketTypes(set);
    }

    private final Set<ProxyPacketType> types;

    public ProxyHandlePacketTypes(Set<ProxyPacketType> types) {
        this.types = Collections.unmodifiableSet(types);
    }

    public Set<ProxyPacketType> getTypes() {
        return types;
    }

    public boolean hasType(ProxyPacketType type) {
        return types.contains(type);
    }

    public enum ProxyPacketType {

        CHAT, SYSTEM_CHAT, ACTIONBAR, TITLE;

    }
}
