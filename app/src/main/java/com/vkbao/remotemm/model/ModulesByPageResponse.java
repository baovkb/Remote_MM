package com.vkbao.remotemm.model;

import java.util.List;

public class ModulesByPageResponse {
    private int page;
    private int totalPage;
    private List<ModulesByPageModel> pageModules;

    public ModulesByPageResponse(int page, List<ModulesByPageModel> pageModules, int totalPage) {
        this.page = page;
        this.pageModules = pageModules;
        this.totalPage = totalPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<ModulesByPageModel> getPageModules() {
        return pageModules;
    }

    public void setPageModules(List<ModulesByPageModel> pageModules) {
        this.pageModules = pageModules;
    }
}
