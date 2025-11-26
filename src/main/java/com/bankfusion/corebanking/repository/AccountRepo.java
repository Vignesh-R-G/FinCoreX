package com.bankfusion.corebanking.repository;

import com.bankfusion.corebanking.entity.Account;
import com.bankfusion.corebanking.entity.Customer;
import com.bankfusion.corebanking.entity.ProductInheritance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account,String> {
    Page<Account> findAllByCustomer(Customer customer, Pageable pageable);

    Page<Account> findAllBySubProduct(ProductInheritance productInheritance, Pageable pageable);
}
