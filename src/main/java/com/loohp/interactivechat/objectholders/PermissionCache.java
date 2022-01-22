package com.loohp.interactivechat.objectholders;

public class PermissionCache {

    private final long time;
    private boolean value;

    public PermissionCache(boolean value, long time) {
        this.value = value;
        this.time = time;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

}
