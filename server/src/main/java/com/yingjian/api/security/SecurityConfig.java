package com.yingjian.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.yingjian.api.model.Models.Envelope;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    static final String ISSUER = "yingjian-feiyi";

    @Bean
    SecretKey jwtKey(@Value("${app.jwt-secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET 至少需要 32 字节");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper json) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, error) -> {
            response.setStatus(401);
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setContentType("application/json;charset=UTF-8");
            json.writeValue(response.getOutputStream(), Envelope.error(401, "请重新登录"));
        };
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/heritage", "/api/heritage/*").permitAll()
                        .anyRequest().hasAuthority("SCOPE_member"))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            json.writeValue(response.getOutputStream(), Envelope.error(403, "无权访问"));
                        }))
                .oauth2ResourceServer(oauth -> oauth.authenticationEntryPoint(unauthorized).jwt(jwt -> {}))
                .build();
    }
}
