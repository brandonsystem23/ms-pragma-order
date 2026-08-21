package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.mapper.RestaurantDtoMapper;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {

    @Mock
    private ICreateRestaurantServicePort createRestaurantUseCase;

    @Mock
    private IListRestaurantsServicePort listRestaurantsUseCase;

    @Mock
    private RestaurantDtoMapper restaurantDtoMapper;

    @InjectMocks
    private RestaurantHandler restaurantApplicationService;

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
                .status(true)
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
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createRestaurantUseCase.create(any(), anyString())).thenReturn(Mono.just(restaurant));
        when(restaurantDtoMapper.toResponse(any(Restaurant.class))).thenReturn(restaurantResponse);
        when(restaurantDtoMapper.toCommand(any())).thenReturn(command);

        StepVerifier.create(restaurantApplicationService.create(request, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Restaurante La 70", response.name());
                    Assertions.assertEquals("123456789", response.nit());
                    Assertions.assertEquals(2L, response.ownerId());
                    Assertions.assertTrue(response.status());
                })
                .verifyComplete();
    }

    @Test
    void shouldListRestaurantsSuccessfully() {
        PageResult<RestaurantListItem> pageResult = PageResult.<RestaurantListItem>builder()
                .content(List.of(
                        RestaurantListItem.builder()
                                .id(1L)
                                .name("Burger House")
                                .urlLogo("https://logo.com/burger.png")
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        RestaurantListItemResponse restaurant = RestaurantListItemResponse.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://example.com/logo.png")
                .build();

        when(listRestaurantsUseCase.list(anyString(), anyInt(), anyInt()))
                .thenReturn(Mono.just(pageResult));
        when(restaurantDtoMapper.toResponse(any(RestaurantListItem.class))).thenReturn(restaurant);

        StepVerifier.create(restaurantApplicationService.list("token-test", 0, 10))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals("Burger House", response.content().getFirst().name());
                    Assertions.assertEquals(1L, response.totalElements());
                })
                .verifyComplete();
    }
}
