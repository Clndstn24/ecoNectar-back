package com.econectar.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String email;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String postalCode;
}