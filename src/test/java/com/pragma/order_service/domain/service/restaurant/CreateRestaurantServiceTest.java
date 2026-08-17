package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRestaurantServiceTest {

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private RestaurantRegistrationValidator restaurantRegistrationValidator;

    @Mock
    private RestaurantDomainValidator restaurantDomainValidator;

    @InjectMocks
    private CreateRestaurantService service;


    @Test
    void shouldCreateRestaurantSuccessfully() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante La 70",
                "123456789",
                "Calle 10 # 20-30",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        Restaurant savedRestaurant = Restaurant.builder()
                .id(1L)
                .name(command.name())
                .nit(command.nit())
                .address(command.address())
                .phone(command.phone())
                .urlLogo(command.urlLogo())
                .ownerId(command.ownerId())
                .build();

        doNothing().when(restaurantDomainValidator).validateForCreate(any());
        when(restaurantRegistrationValidator.validate(anyString(), anyLong(), anyString()))
                .thenReturn(Mono.empty());
        when(restaurantPersistencePort.save(any()))
                .thenReturn(Mono.just(savedRestaurant));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals("Restaurante La 70", result.getName());
                    Assertions.assertEquals("123456789", result.getNit());
                    Assertions.assertEquals(2L, result.getOwnerId());
                })
                .verifyComplete();

    }
}
