package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.infrastructure.output.postgres.entity.RestaurantEntity;
import com.pragma.order_service.infrastructure.output.postgres.mapper.RestaurantEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.RestaurantRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantPersistenceAdapterTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantEntityMapper restaurantEntityMapper;

    @InjectMocks
    private RestaurantPersistenceAdapter restaurantPersistenceAdapter;

    @Test
    void shouldReturnTrueWhenNitExists() {
        when(restaurantRepository.existsByNit(anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(restaurantPersistenceAdapter.existsByNit("123456789"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldSaveRestaurantSuccessfully() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Restaurante La 70")
                .nit("123456789")
                .address("Calle 10 # 20-30")
                .phone("+573005698325")
                .urlLogo("https://logo.com/logo.png")
                .ownerId(2L)
                .status(true)
                .build();

        RestaurantEntity entity = RestaurantEntity.builder()
                .id(1L)
                .name("Restaurante La 70")
                .nit("123456789")
                .address("Calle 10 # 20-30")
                .phone("+573005698325")
                .urlLogo("https://logo.com/logo.png")
                .ownerId(2L)
                .status(true)
                .build();

        when(restaurantRepository.save(any()))
                .thenReturn(Mono.just(entity));

        when(restaurantEntityMapper.toEntity(any()))
                .thenReturn(entity);

        when(restaurantEntityMapper.toDomain(any()))
                .thenReturn(restaurant);

        StepVerifier.create(restaurantPersistenceAdapter.save(restaurant))
                .assertNext(saved -> {
                    Assertions.assertEquals(1L, saved.getId());
                    Assertions.assertEquals("Restaurante La 70", saved.getName());
                    Assertions.assertTrue(saved.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnTrueWhenRestaurantExistsByOwner() {
        when(restaurantRepository.existsByIdAndOwnerId(1L, 2L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(restaurantPersistenceAdapter.existByOwner(1L, 2L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnTrueWhenRestaurantExistsById() {
        when(restaurantRepository.existsById(1L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(restaurantPersistenceAdapter.existById(1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldFindActiveRestaurantsOrdered() {
        RestaurantEntity entity1 = RestaurantEntity.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .status(true)
                .build();

        RestaurantEntity entity2 = RestaurantEntity.builder()
                .id(2L)
                .name("Pizza Place")
                .urlLogo("https://logo.com/pizza.png")
                .status(true)
                .build();

        Restaurant restaurant1 = Restaurant.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .status(true)
                .build();

        Restaurant restaurant2 = Restaurant.builder()
                .id(2L)
                .name("Pizza Place")
                .urlLogo("https://logo.com/pizza.png")
                .status(true)
                .build();

        when(restaurantRepository.findActiveRestaurantsOrdered(anyInt(), anyInt())).thenReturn(Flux.just(entity1, entity2));
        when(restaurantEntityMapper.toDomain(entity1)).thenReturn(restaurant1);
        when(restaurantEntityMapper.toDomain(entity2)).thenReturn(restaurant2);

        StepVerifier.create(restaurantPersistenceAdapter.findActiveRestaurantsOrdered(0, 10))
                .assertNext(found -> {
                    Assertions.assertEquals("Burger House", found.getName());
                })
                .assertNext(found -> {
                    Assertions.assertEquals("Pizza Place", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldCountActiveRestaurants() {
        when(restaurantRepository.countActiveRestaurants()).thenReturn(Mono.just(2L));

        StepVerifier.create(restaurantPersistenceAdapter.countActiveRestaurants())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void shouldFindRestaurantIdByEmployeeIdSuccessfully() {
        when(restaurantRepository.findRestaurantIdByEmployeeId(30L)).thenReturn(Mono.just(5L));

        StepVerifier.create(restaurantPersistenceAdapter.findRestaurantIdByEmployeeId(30L))
                .expectNext(5L)
                .verifyComplete();
    }

}
