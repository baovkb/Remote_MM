package com.vkbao.remotemm.model;

import java.util.Map;

public class ModuleModel {
    private String module;
    private String position;
    private Map<String, Object> config;

    public ModuleModel(Map<String, Object> config, String position, String module) {
        this.config = config;
        this.position = position;
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
