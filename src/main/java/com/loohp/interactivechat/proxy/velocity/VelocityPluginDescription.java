package com.loohp.interactivechat.proxy.velocity;

import org.json.simple.JSONObject;

public class VelocityPluginDescription {

    private final String id;
    private final String name;
    private final String version;

    public VelocityPluginDescription(JSONObject json) {
        this.id = json.get("id").toString();
        this.name = json.get("name").toString();
        this.version = json.get("version").toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

}
