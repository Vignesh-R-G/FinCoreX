package com.bankfusion.corebanking.repository;

import com.bankfusion.corebanking.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product,String> {
}
