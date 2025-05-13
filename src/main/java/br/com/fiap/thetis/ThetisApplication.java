package br.com.fiap.thetis;

import br.com.fiap.thetis.dto.*;
import br.com.fiap.thetis.service.UserService;
import br.com.fiap.thetis.service.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class ThetisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThetisApplication.class, args);
    }

    /* ---------- Beans compartilhados (todos os perfis) ---------- */

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl() {
            @Override
            public void send(@NonNull SimpleMailMessage msg) {
                System.out.printf("""

                        📧  E-mail para %s
                        Assunto: %s
                        Corpo  : %s

                        """, msg.getTo()[0], msg.getSubject(), msg.getText());
            }
            @Override public void setJavaMailProperties(Properties props) { /* noop */ }
        };
    }

    /* ---------- Runner DEV ---------- */
    @Bean
    @Profile("dev")
    CommandLineRunner devRunner(UserService svc) {
        return args -> {
            System.out.println(">>> Rodando em perfil DEV — criando usuário fake…");
            var u = svc.create(new CreateUserRequest(
                    "devuser", "dev@mail.com", "(11)90000-0000", "12345678909", "123456"));
            System.out.println("Usuário DEV criado: " + u);
            System.out.println("Encerrando aplicação DEV.");
        };
    }

    /* ---------- Runner PROD (CLI interativo) ---------- */
    @Bean
    @Profile("prod")
    CommandLineRunner prodRunner(UserService svc, EmailService mail) {
        return args -> {
            final Scanner SC = new Scanner(System.in);
            System.out.println("=== Thetis CLI (perfil PROD) ===");

            while (true) {
                System.out.print("""
                                    
                        Escolha:
                        [1] Cadastrar
                        [2] Login
                        [3] Solicitar reset senha
                        [4] Confirmar reset senha
                        [0] Sair
                        > """);

                String op = SC.nextLine().trim();

                try {
                    switch (op) {
                        case "1" -> {
                            System.out.print("Username : "); String user = SC.nextLine();
                            System.out.print("E-mail   : "); String email = SC.nextLine();
                            System.out.print("Telefone : "); String phone = SC.nextLine();
                            System.out.print("CPF      : "); String cpf = SC.nextLine();
                            System.out.print("Senha    : "); String pwd = SC.nextLine();

                            var resp = svc.create(new CreateUserRequest(user, email, phone, cpf, pwd));
                            System.out.println("✅  Criado: " + resp);
                        }
                        case "2" -> {
                            System.out.print("User/E-mail: "); String id = SC.nextLine();
                            System.out.print("Senha      : "); String pwd = SC.nextLine();

                            var resp = svc.login(new LoginRequest(id, pwd));
                            System.out.println("✅  Login OK: " + resp);
                        }
                        case "3" -> {
                            System.out.print("E-mail: "); String email = SC.nextLine();
                            svc.requestPasswordReset(new PasswordResetRequest(email));
                            System.out.println("✅  Token enviado (veja console).");
                        }
                        case "4" -> {
                            System.out.print("Token      : "); UUID token = UUID.fromString(SC.nextLine());
                            System.out.print("Nova senha : "); String pwd = SC.nextLine();

                            svc.confirmPasswordReset(new PasswordResetConfirm(token, pwd));
                            System.out.println("✅  Senha alterada.");
                        }
                        case "0" -> System.exit(0);
                        default -> System.out.println("Opção inválida.");
                    }
                } catch (Exception e) {
                    System.out.println("❌  Erro: " + e.getMessage());
                }
            }
        };
    }
}