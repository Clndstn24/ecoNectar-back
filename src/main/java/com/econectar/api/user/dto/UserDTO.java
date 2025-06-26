package com.econectar.api.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String postalCode;
}