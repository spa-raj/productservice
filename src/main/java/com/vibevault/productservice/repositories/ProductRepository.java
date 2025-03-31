package com.vibevault.productservice.repositories;

import com.vibevault.productservice.models.Product;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    <S extends Product> S save(S product);

    @Override
    void delete(Product entity);

    @Override
    void deleteAll(Iterable<? extends Product> entities);

    @Override
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    List<Product> findAll();

    @Override
    Optional<Product> findById(Long productId);
}
