package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.RestaurantListResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.mapper.RestaurantDtoMapper;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {

    @Mock
    private ICreateRestaurantServicePort createRestaurantServicePort;

    @Mock
    private IListRestaurantsServicePort listRestaurantsServicePort;

    @Mock
    private RestaurantDtoMapper restaurantDtoMapper;

    @InjectMocks
    private RestaurantHandler restaurantHandler;

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

        RestaurantResponse response = RestaurantResponse.builder()
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

        when(restaurantDtoMapper.toCommand(request)).thenReturn(command);
        when(createRestaurantServicePort.create(command, "token-test")).thenReturn(Mono.just(restaurant));
        when(restaurantDtoMapper.toResponse(restaurant)).thenReturn(response);

        StepVerifier.create(restaurantHandler.create(request, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals("Restaurante La 70", result.name());
                })
                .verifyComplete();
    }

    @Test
    void shouldListRestaurantsSuccessfully() {
        RestaurantListItem item = RestaurantListItem.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .build();

        PageResult<RestaurantListItem> pageResult = PageResult.<RestaurantListItem>builder()
                .content(List.of(item))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        RestaurantListResponse response = RestaurantListResponse.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .build();

        when(listRestaurantsServicePort.list(0, 10)).thenReturn(Mono.just(pageResult));
        when(restaurantDtoMapper.toResponse(item)).thenReturn(response);

        StepVerifier.create(restaurantHandler.list(0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals("Burger House", result.content().getFirst().name());
                })
                .verifyComplete();
    }
}
