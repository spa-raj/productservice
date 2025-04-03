package com.vibevault.productservice.repositories;

import com.vibevault.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Override
    Optional<Category> findById(UUID id);

    Optional<Category> findByName(String name);

    @Override
    Category save(Category category);

    List<Category> findAllByIdIn(List<UUID> uuids);
}
