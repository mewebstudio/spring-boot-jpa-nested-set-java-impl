package com.mewebstudio.nestedset.dto.request;

public class UpdateCategoryRequest {
    private String name;

    private String parentId;

    public UpdateCategoryRequest() {
    }

    public UpdateCategoryRequest(String name, String parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
