package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.UpdateDishDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishRegistrationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDishUseCaseTest {

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private UpdateDishRegistrationValidator updateDishRegistrationValidator;

    @Mock
    private UpdateDishDomainValidator updateDishDomainValidator;

    @InjectMocks
    private UpdateDishUseCase service;

    @Test
    void shouldUpdateDishSuccessfully() {
        UpdateDishCommand command = new UpdateDishCommand(BigDecimal.valueOf(30000), "Descripción actualizada");

        Dish existingDish = Dish.builder()
                .id(1L)
                .price(BigDecimal.valueOf(25000))
                .description("Anterior")
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .price(BigDecimal.valueOf(30000))
                .description("Descripción actualizada")
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdate(1L, command);
        when(updateDishRegistrationValidator.findActiveDishAndValidateOwnership(1L, 99L))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(existingDish)).thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.update(1L, command, 99L))
                .assertNext(result -> {
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getPrice());
                    Assertions.assertEquals("Descripción actualizada", result.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishStatusSuccessfully() {
        Dish existingDish = Dish.builder()
                .id(1L)
                .status(true)
                .restaurantId(1L)
                .build();

        Dish updatedDish = Dish.builder()
                .id(1L)
                .status(false)
                .restaurantId(1L)
                .build();

        doNothing().when(updateDishDomainValidator).validateForUpdateStatus(1L, false);
        when(updateDishRegistrationValidator.findDishAndValidateOwnership(1L, 99L))
                .thenReturn(Mono.just(existingDish));
        when(dishPersistencePort.save(existingDish)).thenReturn(Mono.just(updatedDish));

        StepVerifier.create(service.updateStatus(1L, false, 99L))
                .assertNext(result -> Assertions.assertFalse(result.getStatus()))
                .verifyComplete();
    }
}
