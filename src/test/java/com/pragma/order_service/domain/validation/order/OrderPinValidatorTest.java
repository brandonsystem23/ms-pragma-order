package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPinValidatorTest {

    @Mock
    private IRedisCachePort iRedisCachePort;

    @InjectMocks
    private OrderPinValidator orderPinValidator;

    @Test
    void shouldValidateDeliveryPinSuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        when(iRedisCachePort.existsByEmployeeDocumentAndPin(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderPinValidator.validateDeliveryPin(session, "151370"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenPinIsInvalid() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        when(iRedisCachePort.existsByEmployeeDocumentAndPin(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(orderPinValidator.validateDeliveryPin(session, "151370"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El PIN de seguridad es inválido", error.getMessage());
                })
                .verify();
    }
}
