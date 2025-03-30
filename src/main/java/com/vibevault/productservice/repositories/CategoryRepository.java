package com.vibevault.productservice.repositories;

import com.vibevault.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // This interface will be used to interact with the database
    // It will extend the JPARepository interface to provide CRUD operations
    // for the Category entity.
    // The Category entity will be defined in the models package.
    // The Category entity will have a Long id as the primary key.

    @Override
    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    @Override
    Category save(Category category);
}
