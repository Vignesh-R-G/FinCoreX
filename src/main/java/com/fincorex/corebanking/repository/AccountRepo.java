package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.Branch;
import com.fincorex.corebanking.entity.Customer;
import com.fincorex.corebanking.entity.ProductInheritance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account,String> {
    Page<Account> findAllByCustomer(Customer customer, Pageable pageable);

    Page<Account> findAllBySubProduct(ProductInheritance productInheritance, Pageable pageable);

    long countByCustomer(Customer customer);

    long countByBranchAndCustomer(Branch branch, Customer customer);
}
