package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPinValidatorTest {

    @Mock
    private IRedisCachePort redisCachePort;

    @InjectMocks
    private OrderPinValidator orderPinValidator;

    @Test
    void shouldValidateDeliveryPinSuccessfully() {
        when(redisCachePort.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderPinValidator.validateDeliveryPin("12345678", "151370"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenPinIsInvalid() {
        when(redisCachePort.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(orderPinValidator.validateDeliveryPin("12345678", "151370"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El PIN de seguridad es inválido", error.getMessage());
                })
                .verify();
    }
}
