package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDishServiceTest {

    @Mock
    private DishPersistencePort dishPersistencePort;

    @Mock
    private DishRegistrationValidator dishRegistrationValidator;

    @Mock
    private DishDomainValidator dishDomainValidator;

    @InjectMocks
    private CreateDishService service;

    @Test
    void shouldCreateDishSuccessfully() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        Dish savedDish = Dish.builder()
                .id(1L)
                .name(command.name())
                .price(command.price())
                .description(command.description())
                .urlImage(command.urlImage())
                .category(command.category())
                .status(true)
                .restaurantId(command.restaurantId())
                .build();

        doNothing().when(dishDomainValidator).validateForCreate(command);
        when(dishRegistrationValidator.validate(anyString(), anyLong(), anyString()))
                .thenReturn(Mono.empty());
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(savedDish));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals("Pizza Hawaiana", result.getName());
                    Assertions.assertEquals(BigDecimal.valueOf(25000), result.getPrice());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertTrue(result.getStatus());
                })
                .verifyComplete();

    }
}

