package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRqDTO {
    private String userName;
    private String password;
    private String roles;
}
