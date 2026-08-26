package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.DishDomainValidator;
import com.pragma.order_service.domain.validation.dish.DishRegistrationValidator;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDishUseCaseTest {

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private DishRegistrationValidator dishRegistrationValidator;

    @Mock
    private DishDomainValidator dishDomainValidator;

    @InjectMocks
    private CreateDishUseCase service;

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
        when(dishRegistrationValidator.validateDishCreationRules(command.name(), command.restaurantId(), 99L))
                .thenReturn(Mono.empty());
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(savedDish));

        StepVerifier.create(service.create(command, 99L))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals("Pizza Hawaiana", result.getName());
                })
                .verifyComplete();
    }
}
