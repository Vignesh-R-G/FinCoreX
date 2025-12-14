package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product,String> {
}
