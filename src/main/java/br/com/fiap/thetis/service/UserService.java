package br.com.fiap.thetis.service;

import br.com.fiap.thetis.dto.*;

public interface UserService {

    UserResponse create(CreateUserRequest req);

    UserResponse login(LoginRequest req);

    void requestPasswordReset(PasswordResetRequest req);

    void confirmPasswordReset(PasswordResetConfirm req);
}