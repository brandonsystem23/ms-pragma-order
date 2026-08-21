package com.pragma.order_service.infrastructure.out.webclient.adapter;

import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.out.webclient.mapper.DomainMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWebClientAdapterTest {

    private MockWebServer mockWebServer;

    private UserWebClientAdapter userWebClientAdapter;

    @Mock
    private DomainMapper domainMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient usersWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        userWebClientAdapter = new UserWebClientAdapter(usersWebClient, "/api/v1/user", domainMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFindUserByIdSuccessfully() {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "id": 2,
                            "firstName": "Juan",
                            "role": "PROPIETARIO"
                        }
                        """));

        UserSummary owner = UserSummary.builder()
                .id(2L)
                .firstName("Juan")
                .status(true)
                .roleName("PROPIETARIO")
                .build();

        when(domainMapper.userToDomain(any())).thenReturn(owner);

        StepVerifier.create(userWebClientAdapter.findById(2L, "token"))
                .assertNext(user -> {
                    Assertions.assertEquals(2L, user.id());
                    Assertions.assertEquals("Juan", user.firstName());
                    Assertions.assertEquals("PROPIETARIO", user.roleName());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenUserNotFound() {

        enqueueErrorResponse(404, null);

        StepVerifier.create(userWebClientAdapter.findById(99L, "token"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;

                    Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

                    Assertions.assertEquals("Error al consultar el usuario", exception.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenClientErrorOccurs() {

        enqueueErrorResponse(400, "Error consultando usuario");

        StepVerifier.create(userWebClientAdapter.findById(2L, "token"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;

                    Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

                    Assertions.assertEquals("Error consultando usuario", exception.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenClientErrorBodyIsEmpty() {

        enqueueErrorResponse(401, null);

        StepVerifier.create(userWebClientAdapter.findById(2L, "token"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;

                    Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

                    Assertions.assertEquals("Error al consultar el usuario", exception.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenServerErrorOccurs() {

        enqueueErrorResponse(500, "Error interno del servicio de usuarios");

        StepVerifier.create(userWebClientAdapter.findById(2L, "token"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;

                    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());

                    Assertions.assertEquals("Error interno del servicio de usuarios", exception.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenServerErrorBodyIsEmpty() {

        enqueueErrorResponse(503, null);

        StepVerifier.create(userWebClientAdapter.findById(2L, "token"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, throwable);

                    ExternalServiceException exception = (ExternalServiceException) throwable;

                    Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

                    Assertions.assertEquals("Error interno del servicio de usuarios", exception.getMessage());
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