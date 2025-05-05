package com.mewebstudio.nestedset.service;

import com.mewebstudio.nestedset.dto.request.CreateCategoryRequest;
import com.mewebstudio.nestedset.dto.request.UpdateCategoryRequest;
import com.mewebstudio.nestedset.entity.Category;
import com.mewebstudio.nestedset.repository.CategoryRepository;
import com.mewebstudio.nestedset.exception.BadRequestException;
import com.mewebstudio.nestedset.exception.NotFoundException;
import com.mewebstudio.springboot.jpa.nestedset.AbstractNestedSetService;
import org.apache.commons.lang3.tuple.Pair;
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
        // Find parent category if parentId is provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));
        }

        // Create node and get left and right values
        Pair<Integer, Integer> nodePositions = createNode(categoryRepository.findAllOrderedByLeft(), parent);
        int newLeft = nodePositions.getLeft();
        int newRight = nodePositions.getRight();

        // Save and return the new category
        Category newCategory = new Category(request.getName(), newLeft, newRight, parent);
        Category savedCategory = categoryRepository.save(newCategory);
        log.info("Created: {}", savedCategory);
        return savedCategory;
    }

    /**
     * Delete a category and its subtree.
     *
     * @param id String The ID of the category to delete.
     * @throws NotFoundException if the category is not found.
     */
    @Transactional
    public void delete(String id) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Category not found"));
        int width = category.getRight() - category.getLeft() + 1;

        // Delete the subtree
        List<Category> subtree = categoryRepository.findSubtree(category.getLeft(), category.getRight());
        categoryRepository.deleteAll(subtree);
        closeGapInTree(category, width, categoryRepository.findAllOrderedByLeft());
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

    /**
     * Update the name and/or parent of a category.
     *
     * @param id      String The ID of the category to update.
     * @param request UpdateCategoryRequest The request containing the new name and optional parent ID.
     * @return Category The updated category.
     * @throws NotFoundException   if the category is not found.
     * @throws BadRequestException if the category is not found or if a cyclic reference is detected.
     */
    @Transactional
    public Category update(String id, UpdateCategoryRequest request) {
        // Fetch the category to update
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));

        // Set the new name
        category.setName(request.getName());

        // Handle parent change if parentId is provided
        if (request.getParentId() != null) {
            // Determine the new parent
            Category newParent = request.getParentId().isEmpty() ? null :
                categoryRepository.findById(request.getParentId()).orElseThrow(() -> new NotFoundException("New parent not found"));

            // Check for cyclic reference and move category
            if (newParent != null && isDescendant(category, newParent)) {
                throw new BadRequestException("Cannot move category under its own descendant");
            }

            int distance = category.getRight() - category.getLeft() + 1;
            List<Category> allCategories = categoryRepository.findAllOrderedByLeft();
            closeGapInTree(category, distance, allCategories);

            // Calculate new left and right positions
            Pair<Integer, Integer> nodePositions = createNode(allCategories, newParent);
            int newLeft = nodePositions.getLeft();
            int newRight = nodePositions.getRight();

            // Update category with new parent and position
            category.setParent(newParent);
            category.setLeft(newLeft);
            category.setRight(newRight);
        }

        // Save and return updated category
        Category savedCategory = categoryRepository.save(category);
        log.info("Updated: {}", savedCategory);
        return savedCategory;
    }
}
