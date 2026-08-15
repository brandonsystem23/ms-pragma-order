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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
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
                .build();

        RestaurantEntity entity = RestaurantEntity.builder()
                .id(1L)
                .name("Restaurante La 70")
                .nit("123456789")
                .address("Calle 10 # 20-30")
                .phone("+573005698325")
                .urlLogo("https://logo.com/logo.png")
                .ownerId(2L)
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
                })
                .verifyComplete();
    }
}
