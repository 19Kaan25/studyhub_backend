package htw.webtech.studyhub.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Stellt den BCrypt-PasswordEncoder als Bean bereit.
 * Wird in Schritt 3 (SecurityConfig) ebenfalls verwendet – nicht erneut definieren.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
