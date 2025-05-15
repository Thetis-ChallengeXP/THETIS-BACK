package br.com.fiap.thetis;

import br.com.fiap.thetis.dto.*;
import br.com.fiap.thetis.service.EmailService;
import br.com.fiap.thetis.service.UserService;
import io.github.cdimascio.dotenv.Dotenv;

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
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        SpringApplication.run(ThetisApplication.class, args);
    }

    /* ----------------- Beans comuns ----------------- */

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /* ----------------- Mail FAKE (DEV) --------------- */
    @Bean
    @Profile("dev")
    JavaMailSender javaMailSenderFake() {
        return new JavaMailSenderImpl() {
            @Override public void send(@NonNull SimpleMailMessage msg) {
                System.out.printf("""
                                   📧 [FAKE] Para %s
                                   Assunto: %s
                                   Corpo  : %s
                                   """, msg.getTo()[0], msg.getSubject(), msg.getText());
            }
            @Override public void setJavaMailProperties(Properties p) { }
        };
    }

    /* ----------------- Runner DEV ------------------- */
    @Bean
    @Profile("dev")
    CommandLineRunner devRunner(UserService svc) {
        return a -> {
            var u = svc.create(new CreateUserRequest(
                    "dev", "dev@mail.com", "000", "12345678909", "123456"));
            System.out.println("Usuário DEV: " + u);
        };
    }

    /* ----------------- Runner PROD (CLI) ------------- */
    @Bean
    @Profile("prod")
    CommandLineRunner prodRunner(UserService svc, EmailService mail) {
        return a -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("""
                        [1] Cadastrar  [2] Login
                        [3] Pedir reset [4] Confirmar reset  [0] Sair
                        > """);
                switch (sc.nextLine().trim()) {
                    case "1" -> {
                        System.out.print("User: "); String u = sc.nextLine();
                        System.out.print("Mail: "); String e = sc.nextLine();
                        System.out.print("Tel : "); String t = sc.nextLine();
                        System.out.print("CPF : "); String c = sc.nextLine();
                        System.out.print("Pass: "); String p = sc.nextLine();
                        System.out.println(svc.create(new CreateUserRequest(u,e,t,c,p)));
                    }
                    case "2" -> {
                        System.out.print("User/mail: "); String id = sc.nextLine();
                        System.out.print("Pass     : "); String p = sc.nextLine();
                        System.out.println(svc.login(new LoginRequest(id,p)));
                    }
                    case "3" -> {
                        System.out.print("E-mail: "); String e = sc.nextLine();
                        svc.requestPasswordReset(new PasswordResetRequest(e));
                    }
                    case "4" -> {
                        System.out.print("Token: "); UUID tok = UUID.fromString(sc.nextLine());
                        System.out.print("Nova senha: "); String p = sc.nextLine();
                        svc.confirmPasswordReset(new PasswordResetConfirm(tok,p));
                    }
                    case "0" -> System.exit(0);
                }
            }
        };
    }
}