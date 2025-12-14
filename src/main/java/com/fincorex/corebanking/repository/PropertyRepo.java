package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepo extends JpaRepository<Property, String> {
}
