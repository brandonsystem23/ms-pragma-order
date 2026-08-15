package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.infrastructure.output.postgres.entity.UserBasicEntity;
import com.pragma.order_service.infrastructure.output.postgres.mapper.UserBasicEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.UserBasicRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryAdapterTest {

    @Mock
    private UserBasicRepository userBasicRepository;

    @Mock
    private UserBasicEntityMapper userBasicEntityMapper;

    @InjectMocks
    private UserQueryAdapter userQueryAdapter;

    @Test
    void shouldFindUserByIdSuccessfully() {
        UserBasicEntity entity = UserBasicEntity.builder()
                .id(2L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .status(true)
                .roleName("PROPIETARIO")
                .build();

        UserSummary owner = UserSummary.builder()
                .id(2L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .status(true)
                .roleName("PROPIETARIO")
                .build();

        when(userBasicRepository.findUserSummaryById(anyLong()))
                .thenReturn(Mono.just(entity));

        when(userBasicEntityMapper.toDomain(any()))
                .thenReturn(owner);

        StepVerifier.create(userQueryAdapter.findById(2L))
                .assertNext(user -> {
                    Assertions.assertEquals(2L, user.id());
                    Assertions.assertEquals("Juan", user.firstName());
                    Assertions.assertEquals("PROPIETARIO", user.roleName());
                })
                .verifyComplete();
    }
}
