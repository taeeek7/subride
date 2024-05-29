// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/out/adapter/MySubProviderImplComponentTest.java
package com.subride.mysub.infra.out.adapter;

import com.subride.mysub.biz.domain.MySub;
import com.subride.mysub.infra.common.util.TestDataGenerator;
import com.subride.mysub.infra.exception.InfraException;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///mysub",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
public class MySubProviderImplComponentTest {
    @Autowired
    private IMySubRepository mySubRepository;
    private MySubProviderImpl mySubProvider;

    @BeforeEach
    void setup() {
        mySubProvider = new MySubProviderImpl(mySubRepository);
        List<MySubEntity> mySubEntities = TestDataGenerator.generateMySubEntities("user01");
        mySubRepository.saveAll(mySubEntities);
    }

    @AfterEach
    void cleanup() {
        mySubRepository.deleteAll();
    }

    @Test
    void getMySubList_ValidUserId_ReturnMySubList() {
        // Given
        String userId = "user01";

        // When
        List<MySub> mySubList = mySubProvider.getMySubList(userId);

        // Then
        assertThat(mySubList).isNotEmpty();
        assertThat(mySubList.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void cancelSub_ValidUserIdAndSubId_DeleteMySub() {
        // Given
        String userId = "user01";
        Long subId = 1L;
        mySubProvider.subscribeSub(subId, userId);  //--테스트 데이터 등록

        // When
        mySubProvider.cancelSub(subId, userId);

        // Then
        Optional<MySubEntity> deletedMySub = mySubRepository.findByUserIdAndSubId(userId, subId);
        assertThat(deletedMySub).isEmpty();
    }

    @Test
    void cancelSub_InvalidUserIdAndSubId_ThrowInfraException() {
        // Given
        String userId = "invalidUser";
        Long subId = 999L;

        // When, Then
        assertThatThrownBy(() -> mySubProvider.cancelSub(subId, userId))
                .isInstanceOf(InfraException.class)
                .hasMessage("구독 정보가 없습니다.");
    }

    @Test
    void subscribeSub_ValidUserIdAndSubId_SaveMySub() {
        // Given
        String userId = "newUser";
        Long subId = 100L;

        // When
        mySubProvider.subscribeSub(subId, userId);

        // Then
        Optional<MySubEntity> savedMySub = mySubRepository.findByUserIdAndSubId(userId, subId);
        assertThat(savedMySub).isPresent();
        assertThat(savedMySub.get().getUserId()).isEqualTo(userId);
        assertThat(savedMySub.get().getSubId()).isEqualTo(subId);
    }

    @Test
    void isSubscribed_SubscribedUserIdAndSubId_ReturnTrue() {
        // Given
        String userId = "user01";
        Long subId = 900L;
        mySubProvider.subscribeSub(subId, userId);  //--테스트 데이터 등록

        // When
        boolean isSubscribed = mySubProvider.isSubscribed(userId, subId);

        // Then
        assertThat(isSubscribed).isTrue();
    }

    @Test
    void isSubscribed_NotSubscribedUserIdAndSubId_ReturnFalse() {
        // Given
        String userId = "user01";
        Long subId = 999L;

        // When
        boolean isSubscribed = mySubProvider.isSubscribed(userId, subId);

        // Then
        assertThat(isSubscribed).isFalse();
    }
}