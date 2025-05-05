package com.mewebstudio.nestedset.repository;

import com.mewebstudio.nestedset.entity.Category;
import com.mewebstudio.springboot.jpa.nestedset.JpaNestedSetRepository;

public interface CategoryRepository extends JpaNestedSetRepository<Category, String> {
}
