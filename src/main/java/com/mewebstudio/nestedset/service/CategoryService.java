package com.mewebstudio.nestedset.service;

import com.mewebstudio.nestedset.dto.request.CreateCategoryRequest;
import com.mewebstudio.nestedset.dto.request.UpdateCategoryRequest;
import com.mewebstudio.nestedset.entity.Category;
import com.mewebstudio.nestedset.repository.CategoryRepository;
import com.mewebstudio.nestedset.exception.BadRequestException;
import com.mewebstudio.nestedset.exception.NotFoundException;
import com.mewebstudio.springboot.jpa.nestedset.AbstractNestedSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService extends AbstractNestedSetService<Category, String> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
        log.debug("CategoryService initialized with repository: {}", categoryRepository);
        if (categoryRepository == null) {
            throw new IllegalArgumentException("CategoryRepository cannot be null");
        }
    }

    /**
     * Retrieve all categories ordered by left value.
     *
     * @return List<Category> The list of all categories.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderedByLeft();
    }

    /**
     * Retrieve a category by ID.
     *
     * @param id String The ID of the category.
     * @return Category The category with the specified ID.
     * @throws NotFoundException if the category is not found.
     */
    public Category findById(String id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
    }

    /**
     * Rebuild the tree structure of categories.
     *
     * @param category Category? The root category to start rebuilding from.
     */
    @Transactional
    public void rebuildTree(Category category) {
        rebuildTree(category, categoryRepository.findAllOrderedByLeft());
    }

    /**
     * Create a new category with the specified name and optional parent.
     *
     * @param request CreateCategoryRequest The request containing the category name and optional parent ID.
     * @return Category The created category.
     * @throws NotFoundException if the parent category is not found.
     */
    @Transactional
    public Category create(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name " + request.getName() + " already exists");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));
        }

        Category category = new Category(request.getName(), 0, 0, parent);
        return createNode(category);
    }

    /**
     * Update the name and/or parent of a category.
     *
     * @param id      The ID of the category to update.
     * @param request The request containing the new name and optional parent ID.
     * @return The updated category.
     * @throws NotFoundException   if the category or parent category is not found.
     * @throws BadRequestException if a cyclic reference is detected.
     */
    @Transactional
    public Category update(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found"));

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));
        }

        category.setName(request.getName());

        return updateNode(category, parent);
    }

    /**
     * Delete a category and its subtree.
     *
     * @param id String The ID of the category to delete.
     * @throws NotFoundException if the category is not found.
     */
    @Transactional
    public void delete(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found"));
        deleteNode(category);
        log.info("Deleted: {}", id);
    }

    /**
     * Retrieve the subtree under the specified category.
     *
     * @param id String The ID of the category.
     * @return List<Category> The list of categories in the subtree.
     * @throws NotFoundException if the category is not found.
     */
    public List<Category> getSubtree(String id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        return categoryRepository.findSubtree(category.getLeft(), category.getRight());
    }
}
