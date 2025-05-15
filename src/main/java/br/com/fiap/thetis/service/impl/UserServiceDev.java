package br.com.fiap.thetis.service.impl;

import br.com.fiap.thetis.dto.*;
import br.com.fiap.thetis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Profile("dev")
public class UserServiceDev implements UserService {

    @Override
    public UserResponse create(CreateUserRequest req) {
        log.info("[DEV] Usuário FAKE criado: {}", req.username());
        return new UserResponse(UUID.randomUUID(), req.username(), req.email(), req.phone(), req.cpf());
    }

    @Override
    public UserResponse login(LoginRequest req) {
        log.info("[DEV] Login FAKE para {}", req.usernameOrEmail());
        return new UserResponse(UUID.randomUUID(), req.usernameOrEmail(), req.usernameOrEmail(), "", "");
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest req) {
        log.info("[DEV] Reset de senha FAKE enviado para {}", req.email());
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirm req) {
        log.info("[DEV] Token FAKE {} consumido — nova senha definida", req.token());
    }
}