package com.econectar.api.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    @Email(message = "El correo no tiene formato válido")
    private String email;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
