package com.fincorex.corebanking.config;

import com.fincorex.corebanking.entity.UserInfo;
import com.fincorex.corebanking.repository.UserInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserInfoRepo repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userInfo=repository.findById(username);
        return userInfo.map(UserInfoUserDetails::new).orElseThrow(()->new UsernameNotFoundException("User Not Found"));
    }

}