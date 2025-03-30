package com.vibevault.productservice.repositories;

import com.vibevault.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    <S extends Product> S save(S product);

    @Override
    void delete(Product entity);

    @Override
    void deleteAll(Iterable<? extends Product> entities);
}
