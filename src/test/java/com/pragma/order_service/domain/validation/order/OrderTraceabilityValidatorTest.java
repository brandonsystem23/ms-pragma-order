package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTraceabilityValidatorTest {

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @InjectMocks
    private OrderTraceabilityValidator orderTraceabilityValidator;

    @Test
    void shouldReturnEmptyWhenOrderDetailsAreEmpty() {
        StepVerifier.create(orderTraceabilityValidator.validateAndGetRestaurant(List.of()))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenOrderDetailsAreNull() {
        StepVerifier.create(orderTraceabilityValidator.validateAndGetRestaurant(null))
                .verifyComplete();
    }

    @Test
    void shouldValidateAndGetRestaurantSuccessfully() {
        OrderDetail detail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(5L)
                .restaurantName("El Buen Sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(15000))
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.ONE)
                .dishPrice(BigDecimal.valueOf(15000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(5L)
                .name("El Buen Sabor")
                .ownerId(99L)
                .build();

        when(iRestaurantPersistencePort.findById(anyLong())).thenReturn(Mono.just(restaurant));

        StepVerifier.create(orderTraceabilityValidator.validateAndGetRestaurant(List.of(detail)))
                .assertNext(result -> {
                    Assertions.assertEquals(5L, result.getId());
                    Assertions.assertEquals("El Buen Sabor", result.getName());
                    Assertions.assertEquals(99L, result.getOwnerId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        OrderDetail detail = OrderDetail.builder()
                .restaurantId(5L)
                .build();

        when(iRestaurantPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(orderTraceabilityValidator.validateAndGetRestaurant(List.of(detail)))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }
}
