package com.loohp.interactivechat.objectholders;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class ICPlaceholder {

    protected Pattern keyword;
    protected String name;
    protected String description;
    protected String permission;
    protected long cooldown;
    protected UUID internalId;

    public ICPlaceholder(Pattern keyword, String name, String description, String permission, long cooldown) {
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.cooldown = cooldown;
        this.internalId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    public abstract boolean isBuildIn();

    public String getPermission() {
        return permission;
    }

    public Pattern getKeyword() {
        return keyword;
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getInternalId() {
        return internalId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalId == null) ? 0 : internalId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ICPlaceholder other = (ICPlaceholder) obj;
        if (internalId == null) {
            return other.internalId == null;
        } else return internalId.equals(other.internalId);
    }

}
