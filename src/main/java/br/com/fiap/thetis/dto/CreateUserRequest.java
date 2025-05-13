package br.com.fiap.thetis.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String phone,
        @CPF String cpf,
        @Size(min = 6) String password
) {}
