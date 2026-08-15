package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.service.RestaurantApplicationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private RestaurantApplicationService restaurantApplicationService;

    @InjectMocks
    private RestaurantController restaurantController;

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

        RestaurantResponse response = RestaurantResponse.builder()
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


        when(restaurantApplicationService.create(any(), anyString()))
                    .thenReturn(Mono.just(response));

        StepVerifier.create(restaurantController.create("Bearer token-test", request))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals("Restaurante La 70", result.name());
                })
                .verifyComplete();

    }
}
