package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.AuthRequestDTO;
import com.fincorex.corebanking.dto.AuthResponseDTO;
import com.fincorex.corebanking.dto.CreateUserRqDTO;
import com.fincorex.corebanking.dto.RefreshTokenRqDTO;
import com.fincorex.corebanking.entity.RefreshToken;
import com.fincorex.corebanking.entity.UserInfo;
import com.fincorex.corebanking.enums.Roles;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.repository.RefreshTokenRepo;
import com.fincorex.corebanking.repository.UserInfoRepo;
import com.fincorex.corebanking.service.impl.JwtServiceImpl;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private JwtServiceImpl jwtService;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    @PostMapping("/createUser")
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRqDTO createUserRqDTO) throws BadRequestException {
        String[] roles = createUserRqDTO.getRoles().split(",");
        List<String> eligibleRoles = Arrays.stream(Roles.values()).map(Enum::name).toList();
        for(String role : roles){
            if(!eligibleRoles.contains(role)){
                throw new BadRequestException("Provided Roles does not meet the eligible role criteria");
            }
        }

        UserInfo userInfo = UserInfo.builder()
                .userName(createUserRqDTO.getUserName())
                .password(passwordEncoder.encode(createUserRqDTO.getPassword()))
                .authorities(createUserRqDTO.getRoles())
                .build();
        userInfoRepo.save(userInfo);
        return ResponseHandler.generateSuccessResponse("User created successfully", HttpStatus.CREATED);
    }

    @PostMapping("/loginUser")
    public ResponseEntity<Object> loginUser(@RequestBody AuthRequestDTO authRequest){
        String userName = authRequest.getUserName();
        String password = authRequest.getPassword();
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName,password));
        if(authentication.isAuthenticated()) {
            UserInfo userInfo = userInfoRepo.findById(authRequest.getUserName()).get();
            List<RefreshToken> refreshTokens = refreshTokenRepo.findAllByUserInfo(userInfo);
            if(!refreshTokens.isEmpty()){
                refreshTokenRepo.deleteAll(refreshTokens);
            }

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .userInfo(userInfoRepo.findById(authRequest.getUserName()).get())
                    .expiryDate(Instant.now().plusMillis(60*60*1000))
                    .build();
            refreshToken = refreshTokenRepo.save(refreshToken);

            String authToken = jwtService.generateToken(authRequest.getUserName());

            AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                    .authToken(authToken)
                    .refreshToken(refreshToken.getToken())
                    .build();

            return ResponseHandler.generateSuccessResponse(authResponseDTO, HttpStatus.OK);
        }
        return ResponseHandler.generateFailureResponse("Invalid Credentials", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/generateTokenUsingRefreshToken")
    public ResponseEntity<Object> generateTokenUsingRefreshToken(@RequestBody RefreshTokenRqDTO refreshTokenRqDTO){
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenRqDTO.getRefreshToken());
        if(refreshToken == null || Instant.now().compareTo(refreshToken.getExpiryDate()) >0){
            return ResponseHandler.generateFailureResponse("Invalid Refresh Token or Refresh Token is Expired", HttpStatus.UNAUTHORIZED);
        }

        String authToken = jwtService.generateToken(refreshToken.getUserInfo().getUserName());

        AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                .authToken(authToken)
                .refreshToken(refreshToken.getToken())
                .build();

        return ResponseHandler.generateSuccessResponse(authResponseDTO, HttpStatus.OK);
    }
}
