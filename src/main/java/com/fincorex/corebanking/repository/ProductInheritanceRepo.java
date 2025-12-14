package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Product;
import com.fincorex.corebanking.entity.ProductInheritance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductInheritanceRepo extends JpaRepository<ProductInheritance,String> {
    List<ProductInheritance> findByProduct(Product product);

    List<ProductInheritance> findAllByIsoCurrencyCode(String currency);

    Page<ProductInheritance> findAllByProduct(Product product, Pageable pageable);

    List<ProductInheritance> findAllByPnlAccount(String pnlAccount);

    List<ProductInheritance> findAllByGlAccount(String glAccount);
}
