package com.mewebstudio.nestedset.dto.response;

import com.mewebstudio.nestedset.entity.Category;
import com.mewebstudio.springboot.jpa.nestedset.INestedSetNodeResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryResponse extends AbstractBaseResponse implements INestedSetNodeResponse<String> {
    private String id;

    private String name;

    private int left;

    private int right;

    private CategoryResponse parent;

    private List<CategoryResponse> children;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CategoryResponse(String id, String name, int left, int right, CategoryResponse parent,
                            List<CategoryResponse> children, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.left = left;
        this.right = right;
        this.parent = parent;
        this.children = children;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    @Override
    public List<INestedSetNodeResponse<String>> getChildren() {
        if (children == null) {
            return List.of();
        }

        return children.stream()
            .map(child -> (INestedSetNodeResponse<String>) child)
            .collect(Collectors.toList());
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }

    public CategoryResponse getParent() {
        return parent;
    }

    public void setParent(CategoryResponse parent) {
        this.parent = parent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public INestedSetNodeResponse<String> withChildren(List<INestedSetNodeResponse<String>> children) {
        List<CategoryResponse> categoryResponses = children.stream()
            .filter(child -> child instanceof CategoryResponse)
            .map(child -> (CategoryResponse) child)
            .collect(Collectors.toList());
        return new CategoryResponse(id, name, left, right, parent, categoryResponses, createdAt, updatedAt);
    }

    public static CategoryResponse convert(Category category, boolean includeParent) {
        CategoryResponse parentResponse = null;
        if (includeParent && category.getParent() != null) {
            parentResponse = convert(category.getParent(), false);
        }

        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getLeft(),
            category.getRight(),
            parentResponse,
            null,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
