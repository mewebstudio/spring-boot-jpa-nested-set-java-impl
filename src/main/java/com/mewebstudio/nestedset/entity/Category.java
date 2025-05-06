package com.mewebstudio.nestedset.entity;

import com.mewebstudio.springboot.jpa.nestedset.INestedSetNode;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_categories_name")
)
public class Category extends AbstractBaseEntity implements INestedSetNode<String, Category> {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "left", nullable = false)
    private int left;

    @Column(name = "right", nullable = false)
    private int right;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id")
    private Category parent;

    public Category() {
    }

    public Category(String name, int left, int right, Category parent) {
        this.name = name;
        this.left = left;
        this.right = right;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getLeft() {
        return left;
    }

    @Override
    public void setLeft(int left) {
        this.left = left;
    }

    @Override
    public int getRight() {
        return right;
    }

    @Override
    public void setRight(int right) {
        this.right = right;
    }

    @Override
    public Category getParent() {
        return parent;
    }

    @Override
    public void setParent(INestedSetNode<String, Category> parent) {
        this.parent = (Category) parent;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
            + "(id = " + getId()
            + ", name = " + name
            + ", left = " + left
            + ", right = " + right
            + ", parent = " + (parent != null ? parent.getId() : null)
            + ")";
    }
}
