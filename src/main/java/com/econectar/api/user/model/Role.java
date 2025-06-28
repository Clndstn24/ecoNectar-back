package com.econectar.api.user.model;

import org.springframework.security.core.GrantedAuthority;


public enum Role implements GrantedAuthority {
    ADMIN,
    USER,
    SELLER,
    CUSTOMER;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
