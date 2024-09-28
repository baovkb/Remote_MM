package com.vkbao.remotemm.model;

public class ModulesByPageModel {
    private String name;
    private String identifier;
    private boolean hidden;

    public ModulesByPageModel(String name, String identifier, boolean hidden) {
        this.name = name;
        this.identifier = identifier;
        this.hidden = hidden;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
