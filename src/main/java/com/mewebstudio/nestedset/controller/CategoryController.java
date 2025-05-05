package com.mewebstudio.nestedset.controller;

import com.mewebstudio.nestedset.dto.request.CreateCategoryRequest;
import com.mewebstudio.nestedset.dto.request.UpdateCategoryRequest;
import com.mewebstudio.nestedset.dto.response.CategoryResponse;
import com.mewebstudio.nestedset.entity.Category;
import com.mewebstudio.nestedset.service.CategoryService;
import com.mewebstudio.nestedset.exception.BadRequestException;
import com.mewebstudio.springboot.jpa.nestedset.NestedSetUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> tree() {
        return ResponseEntity.ok(
            NestedSetUtil.tree(
                categoryService.getAllCategories(),
                category -> CategoryResponse.convert(category, false)
            )
        );
    }

    @GetMapping("/ancestors/{id}")
    public ResponseEntity<List<CategoryResponse>> ancestorsById(@PathVariable String id) {
        return ResponseEntity.ok(
            NestedSetUtil.tree(
                categoryService.getAncestors(categoryService.findById(id)),
                category -> CategoryResponse.convert(category, false)
            )
        );
    }

    @GetMapping("/descendants/{id}")
    public ResponseEntity<List<CategoryResponse>> descendantsById(@PathVariable String id) {
        return ResponseEntity.ok(
            NestedSetUtil.tree(
                categoryService.getDescendants(categoryService.findById(id)),
                category -> CategoryResponse.convert(category, false)
            )
        );
    }

    /**
     * Create a new category.
     *
     * @param request The request containing the category name and optional parent ID.
     * @return The created category.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.convert(categoryService.create(request), true));
    }

    /**
     * Show a category by ID.
     *
     * @param id The ID of the category to show.
     * @return The category with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> show(@PathVariable String id) {
        return ResponseEntity.ok(CategoryResponse.convert(categoryService.findById(id), true));
    }

    /**
     * Move a category up or down in the tree.
     *
     * @param id     The ID of the category to move.
     * @param action The action to perform ("up" or "down").
     * @return The updated category.
     */
    @PostMapping("/{id}/{action}")
    public ResponseEntity<CategoryResponse> move(@PathVariable String id, @PathVariable String action) {
        Category category = categoryService.findById(id);

        return ResponseEntity.ok(
            switch (action) {
                case "up" -> CategoryResponse.convert(categoryService.moveUp(category), true);
                case "down" -> CategoryResponse.convert(categoryService.moveDown(category), true);
                default -> throw new BadRequestException("Invalid action: " + action);
            }
        );
    }

    /**
     * Update a category's name and/or parent.
     *
     * @param id      The ID of the category to update.
     * @param request The request containing the new name and optional parent ID.
     * @return The updated category.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(CategoryResponse.convert(categoryService.update(id, request), true));
    }

    @PatchMapping
    public ResponseEntity<Void> rebuild() {
        categoryService.rebuildTree(null);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a category and its subtree.
     *
     * @param id The ID of the category to delete.
     * @return No content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the subtree of a category.
     *
     * @param id The ID of the category.
     * @return The subtree as a list of categories.
     */
    @GetMapping("/{id}/subtree")
    public ResponseEntity<List<Category>> getSubtree(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.getSubtree(id));
    }
}
