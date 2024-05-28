package com.subride.subrecommend.infra.out.adapter;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.infra.common.config.SecurityConfig;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import com.subride.subrecommend.infra.common.util.TestDataGenerator;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
데이터 레포지토리 컨포넌트 테스트 예시
- 목적: 데이터 CRUD 테스트
- 방법: 실제 데이터베이스를 테스트 컨테이너로 실행하여 테스트
 */
@DataJpaTest    //Entity, Repository, JPA관련 설정만 로딩하여 데이터 액세스 테스트를 지원함
//-- @DataJpaTest는 기본으로 내장 데이터베이스인 H2를 사용함. 이 테스트 DB를 사용하지 않겠다는 설정임
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/*
테스트 데이터베이스 설정: 컨테이너로 서비스에 사용하는 DB와 동일한 DB를 이용하도록 설정함
- driver-class-name: 컨테이너화된 DB사용을 위한 DB driver 설정
- url: 'jdbc:tc'뒤의 Mysql:8.0.29는 docker hub에 있는 image이름임.
        '//'뒤에는 hostname을 지정하는데 빈 값이면 랜덤으로 지정됨
        만약 docker hub외의 Image registry를 사용한다면 image path를 지정할 때 full path를 써주면 됨
        전체경로 구성: {registry}/{organization}/{repository}:{tag}
        예) myharbor.io/database/mysql:8.0.29
- username, password: DB에 접속할 계정정보인데 아무거나 지정하면 됨
- jpa.database-platform: DB엔진에 따른 Hibernate 유형 지정
 */
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///subrecommend",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})

/*
@DataJpaTest는 데이터 관련된 Bean만 로딩하므로 추가로 필요한 클래스는 Import 해 줘야 함
먼저 필요한 클래스를 추가하고 실행 시 에러 메시지를 보면서 추가해 나가면 됨
 */
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class SubRecommendProviderImplComponentTest {
    private final ISpendingRepository spendingRepository;
    private final ICategoryRepository categoryRepository;
    private final ISubRepository subRepository;

    private SubRecommendProviderImpl subRecommendProvider;

    @Autowired
    public SubRecommendProviderImplComponentTest(ISpendingRepository spendingRepository, ICategoryRepository categoryRepository, ISubRepository subRepository) {
        this.spendingRepository = spendingRepository;
        this.categoryRepository = categoryRepository;
        this.subRepository = subRepository;
    }

    @BeforeEach
    void setup() {
        subRecommendProvider = new SubRecommendProviderImpl(spendingRepository, categoryRepository, subRepository);

        cleanup();  //테스트 데이터 모두 지움

        List<CategoryEntity> categories = TestDataGenerator.generateCategoryEntities();
        categoryRepository.saveAll(categories);

        List<SubEntity> subs = TestDataGenerator.generateSubEntities(categories);
        subRepository.saveAll(subs);

        //-- Life 소비 카테고리의 지출이 가장 많게 테스트 데이터를 넣음
        SpendingEntity spendingEntity = new SpendingEntity();
        spendingEntity.setUserId("user01");
        spendingEntity.setCategory("Life");
        spendingEntity.setAmount(10000000L);
        spendingRepository.save(spendingEntity);

    }
    @AfterEach
    void cleanup() {
        // 테스트 데이터 삭제
        spendingRepository.deleteAll();
        subRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    //-- 소비가 가장 많은 소비 카테고리와 총액 리턴 테스트
    @Test
    void getSpendingByCategory() {
        //-- Given
        String userId = "user01";

        //-- When
        Map<String, Long> spendingCategory = subRecommendProvider.getSpendingByCategory(userId);

        //-- Then
        assertThat(spendingCategory).containsEntry("Life", 10000000L);
        assertThat(spendingCategory.size()).isEqualTo(1);
    }

    @Test
    void getCategoryBySpendingCategory() {
        //-- Given
        String spendingCategory = "Life";

        //-- When
        Category category = subRecommendProvider.getCategoryBySpendingCategory(spendingCategory);

        //-- Then
        assertThat(category.getCategoryName()).isEqualTo("생필품");
        assertThat(category.getSpendingCategory()).isEqualTo("Life");
    }

    @Test
    void getSubListByCategoryId() {
        //-- Given
        String categoryId = "life";

        //-- When
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);

        //-- Then
        assertThat(subList).isNotEmpty();
        assertThat(subList.get(0).getCategory().getCategoryId()).isEqualTo(categoryId);
    }

}
