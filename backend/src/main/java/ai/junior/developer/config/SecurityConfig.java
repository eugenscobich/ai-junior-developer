package ai.junior.developer.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for simplicity, enable basic HTTP auth
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/.well-known/ai-plugin.json").permitAll(); // allow plugin file
                auth.requestMatchers("/static/**").permitAll(); // allow plugin file
                auth.requestMatchers("/v3/api-docs").permitAll(); // allow plugin file
                auth.requestMatchers("/", "/index.html").permitAll(); // allow home page
                auth.requestMatchers("/api/jira/webhook").permitAll(); // allow home page
                auth.requestMatchers("/api/github/webhook").permitAll(); // allow home page
                auth.anyRequest().authenticated(); // all other requests require authentication
            })
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        UserDetails userDetails = User
            .withUsername("user")
            .passwordEncoder(encoder::encode)
            .password("password")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
}
