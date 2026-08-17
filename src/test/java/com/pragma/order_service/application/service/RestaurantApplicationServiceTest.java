package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.mapper.RestaurantDtoMapper;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantApplicationServiceTest {

    @Mock
    private CreateRestaurantUseCase createRestaurantUseCase;

    @Mock
    private RestaurantDtoMapper restaurantDtoMapper;

    @InjectMocks
    private RestaurantApplicationService restaurantApplicationService;

    @Test
    void shouldCreateRestaurantSuccessfully() {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Restaurante La 70",
                "123456789",
                "Calle 10 # 20-30",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante La 70",
                "123456789",
                "Calle 10 # 20-30",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Restaurante La 70")
                .nit("123456789")
                .address("Calle 10 # 20-30")
                .phone("+573005698325")
                .urlLogo("https://logo.com/logo.png")
                .ownerId(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
                .id(1L)
                .name("Restaurante La 70")
                .nit("123456789")
                .address("Calle 10 # 20-30")
                .phone("+573005698325")
                .urlLogo("https://logo.com/logo.png")
                .ownerId(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createRestaurantUseCase.create(any(), anyString()))
                .thenReturn(Mono.just(restaurant));

        when(restaurantDtoMapper.toResponse(any())).thenReturn(restaurantResponse);
        when(restaurantDtoMapper.toCommand(any())).thenReturn(command);

        StepVerifier.create(restaurantApplicationService.create(request, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Restaurante La 70", response.name());
                    Assertions.assertEquals("123456789", response.nit());
                    Assertions.assertEquals(2L, response.ownerId());
                })
                .verifyComplete();
    }
}
