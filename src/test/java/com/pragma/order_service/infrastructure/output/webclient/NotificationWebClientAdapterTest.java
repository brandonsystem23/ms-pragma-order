package com.pragma.order_service.infrastructure.output.webclient;

import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class NotificationWebClientAdapterTest {

    private MockWebServer mockWebServer;
    private NotificationWebClientAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient notificationsWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        adapter = new NotificationWebClientAdapter(
                notificationsWebClient,
                "/api/v1/notifications/send"
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldSendNotificationSuccessfully() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "phoneNumber": "+51900671048",
                          "message": "Tu pedido está listo"
                        }
                        """));

        StepVerifier.create(adapter.sendReadyNotification("+51900671048", "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals("+51900671048", response.phoneNumber());
                    Assertions.assertEquals("Tu pedido está listo", response.message());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenClientErrorOccurs() {
        enqueueErrorResponse(400, "Error enviando notificación");

        StepVerifier.create(adapter.sendReadyNotification("+51900671048", "token-test"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
                    Assertions.assertEquals("Error enviando notificación", exception.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenServerErrorOccurs() {
        enqueueErrorResponse(500, "Error interno del servicio de notificaciones");

        StepVerifier.create(adapter.sendReadyNotification("+51900671048", "token-test"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;
                    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
                    Assertions.assertEquals("Error interno del servicio de notificaciones", exception.getMessage());
                })
                .verify();
    }

    private void enqueueErrorResponse(int statusCode, String body) {
        for (int i = 0; i < 3; i++) {
            MockResponse response = new MockResponse()
                    .setResponseCode(statusCode);

            if (body != null) {
                response.addHeader("Content-Type", "text/plain")
                        .setBody(body);
            }

            mockWebServer.enqueue(response);
        }
    }
}
