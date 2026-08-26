package com.pragma.order_service.infrastructure.security.session;

import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
import com.pragma.order_service.infrastructure.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class SessionAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.fromCallable(() -> {
            try {
                String token = authentication.getCredentials().toString();
                AuthenticatedUser session = jwtProvider.validateAndGetUser(token);
                return buildAuthentication(session);
            } catch (Exception ex) {
                throw new BadCredentialsException("Token inválido o expirado", ex);
            }
        });
    }

    private Authentication buildAuthentication(AuthenticatedUser authenticatedUser) {
        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.role()))
        );
    }
}
