package com.mewebstudio.nestedset.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCategoryRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String parentId;

    public CreateCategoryRequest() {
    }

    public CreateCategoryRequest(String name, String parentId) {
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
