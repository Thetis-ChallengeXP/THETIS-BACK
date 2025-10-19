package br.com.fiap.thetis.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.thetis.dto.CreateUserRequest;
import br.com.fiap.thetis.dto.LoginRequest;
import br.com.fiap.thetis.dto.PasswordResetConfirm;
import br.com.fiap.thetis.dto.PasswordResetRequest;
import br.com.fiap.thetis.dto.UserResponse;
import br.com.fiap.thetis.exception.BusinessException;
import br.com.fiap.thetis.exception.NotFoundException;
import br.com.fiap.thetis.model.PasswordResetToken;
import br.com.fiap.thetis.model.User;
import br.com.fiap.thetis.repository.PasswordResetTokenRepository;
import br.com.fiap.thetis.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service                       // nenhum @Profile → vale para todos os ambientes
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final EmailService email;

    /* ---------- Cadastro ---------- */
    @Transactional
    public UserResponse create(CreateUserRequest req) {
        validarDuplicidade(req);
        User u = User.builder()
                .username(req.username())
                .email(req.email())
                .phone(req.phone())
                .cpf(req.cpf().replaceAll("\\D", ""))
                .passwordHash(encoder.encode(req.password()))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        userRepo.save(u);
        return map(u);
    }

    /* ---------- Login ---------- */
    public UserResponse login(LoginRequest req) {
        User u = buscarPorUsernameOuEmail(req.usernameOrEmail());
        if (!encoder.matches(req.password(), u.getPasswordHash()))
            throw new BusinessException("Senha inválida");
        return map(u);
    }

    /* ---------- Solicitar reset ---------- */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest req) {
    User u = userRepo.findByEmail(req.email())
        .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        PasswordResetToken token = PasswordResetToken.builder()
                .user(u)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        tokenRepo.save(token);

        String link = "https://thetis.app/reset?token=" + token.getId();
        email.send(u.getEmail(), "Recuperação de senha",
                   "Clique no link para redefinir sua senha:\n" + link);
    }

    /* ---------- Confirmar reset ---------- */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirm req) {
        PasswordResetToken token = tokenRepo
                .findByIdAndUsedFalseAndExpiresAtAfter(req.token(), LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));

        User u = token.getUser();
        u.setPasswordHash(encoder.encode(req.newPassword()));
        u.setModifiedAt(LocalDateTime.now());
        token.setUsed(true);
    }

    /* ---------- Helpers ---------- */
    private void validarDuplicidade(CreateUserRequest req) {
        if (userRepo.findByEmail(req.email()).isPresent())
            throw new BusinessException("E-mail já cadastrado");
        if (userRepo.findByUsername(req.username()).isPresent())
            throw new BusinessException("Username já cadastrado");
    }

    private User buscarPorUsernameOuEmail(String usernameOrEmail) {
        return userRepo.findByEmail(usernameOrEmail)
                       .or(() -> userRepo.findByUsername(usernameOrEmail))
                       .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    private static UserResponse map(User u) {
        return new UserResponse(u.getId(), u.getUsername(),
                                u.getEmail(), u.getPhone(), u.getCpf());
    }
}