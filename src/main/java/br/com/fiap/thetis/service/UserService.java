package br.com.fiap.thetis.service;

import br.com.fiap.thetis.dto.*;
import br.com.fiap.thetis.model.*;
import br.com.fiap.thetis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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
            throw new IllegalArgumentException("Senha inválida");
        return map(u);
    }

    /* ---------- Solicitar reset ---------- */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest req) {
        User u = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
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
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        User u = token.getUser();
        u.setPasswordHash(encoder.encode(req.newPassword()));
        u.setModifiedAt(LocalDateTime.now());
        token.setUsed(true);
    }

    /* ---------- Helpers ---------- */
    private void validarDuplicidade(CreateUserRequest req) {
        if (userRepo.findByEmail(req.email()).isPresent())
            throw new IllegalArgumentException("E-mail já cadastrado");
        if (userRepo.findByUsername(req.username()).isPresent())
            throw new IllegalArgumentException("Username já cadastrado");
    }

    private User buscarPorUsernameOuEmail(String usernameOrEmail) {
        return userRepo.findByEmail(usernameOrEmail)
                       .or(() -> userRepo.findByUsername(usernameOrEmail))
                       .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    private static UserResponse map(User u) {
        return new UserResponse(u.getId(), u.getUsername(),
                                u.getEmail(), u.getPhone(), u.getCpf());
    }
}