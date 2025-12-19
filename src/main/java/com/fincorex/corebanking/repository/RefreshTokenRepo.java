package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.RefreshToken;
import com.fincorex.corebanking.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByUserInfo(UserInfo userInfo);

    RefreshToken findByToken(String refreshToken);
}
