package com.pragma.order_service.infrastructure.out.postgres.adapter;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.infrastructure.out.postgres.entity.DishEntity;
import com.pragma.order_service.infrastructure.out.postgres.mapper.DishEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.repository.DishRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

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

    @Test
    void shouldFindActiveDishesByRestaurantSuccessfully() {
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
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.findActiveByRestaurantId(1L, 10, 0)).thenReturn(Flux.just(entity));
        when(dishEntityMapper.toDomain(entity)).thenReturn(dish);

        StepVerifier.create(dishPersistenceAdapter.findActiveByRestaurantId(1L, 0, 10))
                .assertNext(found -> {
                    Assertions.assertEquals(1L, found.getId());
                    Assertions.assertEquals("Pizza Hawaiana", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindActiveDishesByRestaurantAndCategorySuccessfully() {
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
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.findActiveByRestaurantIdAndCategory(1L, "PIZZA", 10, 0))
                .thenReturn(Flux.just(entity));
        when(dishEntityMapper.toDomain(entity)).thenReturn(dish);

        StepVerifier.create(dishPersistenceAdapter.findActiveByRestaurantIdAndCategory(1L, "PIZZA", 0, 10))
                .assertNext(found -> {
                    Assertions.assertEquals(1L, found.getId());
                    Assertions.assertEquals("PIZZA", found.getCategory());
                })
                .verifyComplete();
    }

    @Test
    void shouldCountActiveDishesByRestaurantSuccessfully() {
        when(dishRepository.countActiveByRestaurantId(1L)).thenReturn(Mono.just(2L));

        StepVerifier.create(dishPersistenceAdapter.countActiveByRestaurantId(1L))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void shouldCountActiveDishesByRestaurantAndCategorySuccessfully() {
        when(dishRepository.countActiveByRestaurantIdAndCategory(1L, "PIZZA")).thenReturn(Mono.just(1L));

        StepVerifier.create(dishPersistenceAdapter.countActiveByRestaurantIdAndCategory(1L, "PIZZA"))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void shouldFindDishesByIdsSuccessfully() {
        Dish dish1 = Dish.builder()
                .id(10L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza1.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish dish2 = Dish.builder()
                .id(11L)
                .name("Hamburguesa Doble")
                .price(BigDecimal.valueOf(18000))
                .description("Hamburguesa con doble carne")
                .urlImage("https://image.com/burger.png")
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        DishEntity entity1 = DishEntity.builder()
                .id(10L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza1.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        DishEntity entity2 = DishEntity.builder()
                .id(11L)
                .name("Hamburguesa Doble")
                .price(BigDecimal.valueOf(18000))
                .description("Hamburguesa con doble carne")
                .urlImage("https://image.com/burger.png")
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        when(dishRepository.findAllById(List.of(10L, 11L)))
                .thenReturn(Flux.just(entity1, entity2));

        when(dishEntityMapper.toDomain(entity1)).thenReturn(dish1);
        when(dishEntityMapper.toDomain(entity2)).thenReturn(dish2);

        StepVerifier.create(dishPersistenceAdapter.findByIds(List.of(10L, 11L)))
                .assertNext(found -> {
                    Assertions.assertEquals(10L, found.getId());
                    Assertions.assertEquals("Pizza Hawaiana", found.getName());
                    Assertions.assertEquals(BigDecimal.valueOf(25000), found.getPrice());
                })
                .assertNext(found -> {
                    Assertions.assertEquals(11L, found.getId());
                    Assertions.assertEquals("Hamburguesa Doble", found.getName());
                    Assertions.assertEquals(BigDecimal.valueOf(18000), found.getPrice());
                })
                .verifyComplete();
    }



}
