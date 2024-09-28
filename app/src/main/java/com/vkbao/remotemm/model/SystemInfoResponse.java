package com.vkbao.remotemm.model;

import java.util.List;

public class SystemInfoResponse {
    private int speaker;
    private int recorder;
    private List<ModuleModel> allModules;
    private ModulesByPageResponse modulesByPage;

    public SystemInfoResponse(int speaker, int recorder, List<ModuleModel> allModules, ModulesByPageResponse modulesByPage) {
        this.speaker = speaker;
        this.recorder = recorder;
        this.allModules = allModules;
        this.modulesByPage = modulesByPage;
    }

    public int getSpeaker() {
        return speaker;
    }

    public ModulesByPageResponse getModulesByPage() {
        return modulesByPage;
    }

    public void setModulesByPage(ModulesByPageResponse modulesByPage) {
        this.modulesByPage = modulesByPage;
    }

    public void setSpeaker(int speaker) {
        this.speaker = speaker;
    }

    public int getRecorder() {
        return recorder;
    }

    public void setRecorder(int recorder) {
        this.recorder = recorder;
    }

    public List<ModuleModel> getAllModules() {
        return allModules;
    }

    public void setAllModules(List<ModuleModel> allModules) {
        this.allModules = allModules;
    }
}
