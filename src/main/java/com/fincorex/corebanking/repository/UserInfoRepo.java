package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepo extends JpaRepository<UserInfo, String> {
}
