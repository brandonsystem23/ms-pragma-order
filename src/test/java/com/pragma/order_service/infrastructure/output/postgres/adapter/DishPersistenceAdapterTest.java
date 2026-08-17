package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.infrastructure.output.postgres.entity.DishEntity;
import com.pragma.order_service.infrastructure.output.postgres.mapper.DishEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.DishRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishPersistenceAdapterTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishEntityMapper dishEntityMapper;

    @InjectMocks
    private DishPersistenceAdapter dishPersistenceAdapter;

    @Test
    void shouldReturnTrueWhenDishNameExists() {
        when(dishRepository.existsByNameIgnoreCase(anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(dishPersistenceAdapter.existsByName("Pizza Hawaiana"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldSaveDishSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        DishEntity entity = DishEntity.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(12))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.save(any()))
                .thenReturn(Mono.just(entity));

        when(dishEntityMapper.toEntity(any()))
                .thenReturn(entity);

        when(dishEntityMapper.toDomain(any()))
                .thenReturn(dish);

        StepVerifier.create(dishPersistenceAdapter.save(dish))
                .assertNext(saved -> {
                    Assertions.assertEquals(1L, saved.getId());
                    Assertions.assertEquals("Pizza Hawaiana", saved.getName());
                    Assertions.assertEquals(BigDecimal.valueOf(25000), saved.getPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindDishByIdAndStatusTrueSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        DishEntity entity = DishEntity.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(15))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.just(entity));
        when(dishEntityMapper.toDomain(any())).thenReturn(dish);

        StepVerifier.create(dishPersistenceAdapter.findByIdAndStatusTrue(1L))
                .assertNext(found -> {
                    Assertions.assertEquals(1L, found.getId());
                    Assertions.assertEquals("Pizza Hawaiana", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindDishByIdSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        DishEntity entity = DishEntity.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(15))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.findById(anyLong())).thenReturn(Mono.just(entity));
        when(dishEntityMapper.toDomain(any())).thenReturn(dish);

        StepVerifier.create(dishPersistenceAdapter.findById(1L))
                .assertNext(found -> {
                    Assertions.assertEquals(1L, found.getId());
                    Assertions.assertEquals("Pizza Hawaiana", found.getName());
                })
                .verifyComplete();
    }

}
