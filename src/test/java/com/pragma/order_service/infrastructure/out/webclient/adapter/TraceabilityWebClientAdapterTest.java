package com.pragma.order_service.infrastructure.out.webclient.adapter;

import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.out.webclient.dto.CreateTraceabilityRequest;
import com.pragma.order_service.infrastructure.out.webclient.mapper.TraceabilityMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceabilityWebClientAdapterTest {

    private MockWebServer mockWebServer;
    private TraceabilityWebClientAdapter adapter;

    @Mock
    private TraceabilityMapper traceabilityMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient traceabilityWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        adapter = new TraceabilityWebClientAdapter(
                traceabilityWebClient,
                "/api/v1/traceability/create",
                traceabilityMapper
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCreateTraceabilitySuccessfully() {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "abc123",
                          "orderId": 14,
                          "customerId": 6,
                          "customerName": "Brandon Rojas",
                          "restaurantId": 4,
                          "restaurantName": "Restaurante 4",
                          "employeeAssignedId": 8,
                          "employeeAssignedName": "Martin Lopez",
                          "ownerRestaurant": 5,
                          "status": "ENTREGADO",
                          "description": "Pedido entregado",
                          "changedByUserId": 8,
                          "changedByRole": "EMPLEADO",
                          "changedAt": "2026-08-19T10:49:17.781772"
                        }
                        """));

        Traceability traceability = Traceability.builder()
                .orderId(14L)
                .customerId(6L)
                .customerName("Brandon Rojas")
                .restaurantId(4L)
                .restaurantName("Restaurante 4")
                .ownerRestaurant(5L)
                .employeeAssignedId(8L)
                .employeeAssignedName("Martin Lopez")
                .status("ENTREGADO")
                .description("Pedido entregado")
                .changedByUserId(8L)
                .changedByRole("EMPLEADO")
                .changedAt(LocalDateTime.now())
                .build();

        TraceabilityRecord response = TraceabilityRecord.builder()
                .id("abc123")
                .orderId(14L)
                .customerId(6L)
                .customerName("Brandon Rojas")
                .restaurantId(4L)
                .restaurantName("Restaurante 4")
                .employeeAssignedId(8L)
                .employeeAssignedName("Martin Lopez")
                .ownerRestaurant(5L)
                .status("ENTREGADO")
                .description("Pedido entregado")
                .changedByUserId(8L)
                .changedByRole("EMPLEADO")
                .changedAt(LocalDateTime.now())
                .build();

        CreateTraceabilityRequest request = CreateTraceabilityRequest.builder()
                .build();

        when(traceabilityMapper.toRequest(any(Traceability.class)))
                .thenReturn(request);

        when(traceabilityMapper.toDomain(any()))
                .thenReturn(response);

        StepVerifier.create(
                        adapter.create(traceability, "token-test")
                )
                .assertNext(result -> {
                    Assertions.assertEquals("abc123", result.id());
                    Assertions.assertEquals(14L, result.orderId());
                    Assertions.assertEquals("ENTREGADO", result.status());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenClientErrorOccurs() {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "text/plain")
                .setBody("Error al registrar trazabilidad"));

        CreateTraceabilityRequest request = CreateTraceabilityRequest.builder()
                .build();

        when(traceabilityMapper.toRequest(any(Traceability.class)))
                .thenReturn(request);

        StepVerifier.create(
                        adapter.create(
                                Traceability.builder()
                                        .orderId(1L)
                                        .build(),
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            error
                    );

                    ExternalServiceException ex =
                            (ExternalServiceException) error;

                    Assertions.assertEquals(
                            HttpStatus.BAD_REQUEST,
                            ex.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error al registrar trazabilidad",
                            ex.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenServerErrorOccurs() {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "text/plain")
                .setBody(
                        "Error interno del servicio de trazabilidad"
                ));

        CreateTraceabilityRequest request = CreateTraceabilityRequest.builder()
                .build();

        when(traceabilityMapper.toRequest(any(Traceability.class)))
                .thenReturn(request);

        StepVerifier.create(
                        adapter.create(
                                Traceability.builder()
                                        .orderId(1L)
                                        .build(),
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            error
                    );

                    ExternalServiceException ex =
                            (ExternalServiceException) error;

                    Assertions.assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            ex.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error interno del servicio de trazabilidad",
                            ex.getMessage()
                    );
                })
                .verify();
    }
}