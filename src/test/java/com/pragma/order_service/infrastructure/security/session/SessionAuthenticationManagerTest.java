package com.pragma.order_service.infrastructure.security.session;

import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
import com.pragma.order_service.infrastructure.security.jwt.JwtProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationManagerTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private SessionAuthenticationManager authenticationManager;

    @Test
    void shouldAuthenticateSuccessfully() {

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .userId(1L)
                .fullName("Admin User")
                .role("EMPLEADO")
                .numberDocument("123456")
                .phone("+573001112233")
                .email("admin@test.com")
                .build();

        when(jwtProvider.validateAndGetUser(anyString()))
                .thenReturn(authenticatedUser);


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(null, "jwt-token-123");

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .assertNext(result -> {
                    Assertions.assertTrue(result.isAuthenticated());
                    Assertions.assertEquals(authenticatedUser, result.getPrincipal());

                    Assertions.assertTrue(
                            result.getAuthorities().stream()
                                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_EMPLEADO"))
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(jwtProvider.validateAndGetUser(anyString()))
                .thenThrow(new RuntimeException("Token inválido"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(null, "invalid-token");

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(BadCredentialsException.class)
                .verify();
    }
}
