package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.service.dish.validation.UpdateDishDomainValidator;
import com.pragma.order_service.domain.service.dish.validation.UpdateDishRegistrationValidator;
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
class UpdateDishServiceTest {

    @Mock
    private DishPersistencePort dishPersistencePort;

    @Mock
    private UpdateDishRegistrationValidator updateDishRegistrationValidator;

    @Mock
    private UpdateDishDomainValidator updateDishDomainValidator;

    @InjectMocks
    private UpdateDishService service;

    @Test
    void shouldUpdateDishSuccessfully() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(30000),
                "Descripción actualizada"
        );

        Dish existingDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Descripción anterior")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(30000))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdate(anyLong(), any());
        when(updateDishRegistrationValidator.validate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.update(1L, command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getPrice());
                    Assertions.assertEquals("Descripción actualizada", result.getDescription());
                    Assertions.assertEquals("Pizza Hawaiana", result.getName());
                })
                .verifyComplete();

    }

    @Test
    void shouldUpdateStatusDishSuccessfully() {

        Dish existingDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Descripción anterior")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(30000))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdateStatus(anyLong(), any());
        when(updateDishRegistrationValidator.validate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.updateStatus(1L, false, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getPrice());
                    Assertions.assertEquals("Descripción actualizada", result.getDescription());
                    Assertions.assertEquals("Pizza Hawaiana", result.getName());
                })
                .verifyComplete();

    }

    @Test
    void shouldUpdateOnlyPriceSuccessfully() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(35000),
                null
        );

        Dish existingDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Descripción anterior")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(35000))
                .description("Descripción anterior")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdate(anyLong(), any());
        when(updateDishRegistrationValidator.validate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.update(1L, command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(BigDecimal.valueOf(35000), result.getPrice());
                    Assertions.assertEquals("Descripción anterior", result.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateOnlyDescriptionSuccessfully() {
        UpdateDishCommand command = new UpdateDishCommand(
                null,
                "Nueva descripción"
        );

        Dish existingDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Descripción anterior")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Nueva descripción")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdate(anyLong(), any());
        when(updateDishRegistrationValidator.validate(anyLong(), anyString(), any()))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(any()))
                .thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.update(1L, command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(BigDecimal.valueOf(25000), result.getPrice());
                    Assertions.assertEquals("Nueva descripción", result.getDescription());
                })
                .verifyComplete();
    }


}
