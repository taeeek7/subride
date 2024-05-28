package com.subride.subrecommend.infra.common.util;

import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;

import java.util.*;

public class TestDataGenerator {
    public static List<CategoryEntity> generateCategoryEntities() {
        CategoryEntity category1 = new CategoryEntity("life", "생필품", "Life");
        CategoryEntity category2 = new CategoryEntity("pet", "반려동물", "Pet");
        CategoryEntity category3 = new CategoryEntity("ott", "OTT", "OTT");
        CategoryEntity category4 = new CategoryEntity("food", "Food", "Food");
        CategoryEntity category5 = new CategoryEntity("health", "건강", "Health");
        CategoryEntity category6 = new CategoryEntity("culture", "문화", "Culture");

        return Arrays.asList(category1, category2, category3, category4, category5, category6);
    }

    public static List<SubEntity> generateSubEntities(List<CategoryEntity> categoryEntities) {
        SubEntity sub1 = new SubEntity(1L, "런드리고", "빨래구독 서비스", categoryEntities.get(0), 35000L, 2, "laundrygo.png");
        SubEntity sub2 = new SubEntity(2L, "술담화", "인생 전통술 구독 서비스", categoryEntities.get(3), 39000L, 3, "suldamhwa.jpeg");
        SubEntity sub3 = new SubEntity(3L, "필리", "맞춤형 영양제 정기배송", categoryEntities.get(4), 15000L, 3, "pilly.png");
        SubEntity sub4 = new SubEntity(4L, "넷플릭스", "넷플릭스", categoryEntities.get(2), 15000L, 5, "netflix.png");
        SubEntity sub5 = new SubEntity(5L, "티빙", "티빙", categoryEntities.get(2), 15000L, 5, "tving.png");
        SubEntity sub6 = new SubEntity(6L, "쿠팡플레이", "쿠팡플레이", categoryEntities.get(2), 15000L, 5, "coupang.png");
        SubEntity sub7 = new SubEntity(7L, "디즈니플러스", "디즈니플러스", categoryEntities.get(2), 15000L, 5, "disney.png");
        SubEntity sub8 = new SubEntity(8L, "해피문데이", "생리대 배송 서비스", categoryEntities.get(0), 6000L, 2, "happymoonday.jpeg");
        SubEntity sub9 = new SubEntity(9L, "하비인더박스", "취미용 소품 송 서비스", categoryEntities.get(0), 29000L, 3, "hobbyinthebox.jpeg");
        SubEntity sub10 = new SubEntity(10L, "월간가슴", "맞춤형 브라 배송", categoryEntities.get(0), 16000L, 2, "monthlychest.png");
        SubEntity sub11 = new SubEntity(11L, "위클리셔츠", "깔끔하고 다양한 셔츠 3~5장 매주 배송", categoryEntities.get(0), 40000L, 2, "weeklyshirts.jpeg");
        SubEntity sub12 = new SubEntity(12L, "월간과자", "매월 다른 구성의 과자상자 배송", categoryEntities.get(3), 9900L, 3, "monthlysnack.jpeg");
        SubEntity sub13 = new SubEntity(13L, "밀리의서재", "전자책 무제한 구독", categoryEntities.get(5), 9900L, 5, "milibook.jpeg");
        SubEntity sub14 = new SubEntity(14L, "더 반찬", "맛있고 다양한 집밥반찬 5세트", categoryEntities.get(3), 70000L, 3, "sidedishes.jpeg");
        SubEntity sub15 = new SubEntity(15L, "와이즐리", "면도날 구독 서비스", categoryEntities.get(0), 8900L, 4, "wisely.jpeg");
        SubEntity sub16 = new SubEntity(16L, "미하이 삭스", "매달 패션 양말 3종 배송", categoryEntities.get(0), 990L, 3, "mehi.jpeg");
        SubEntity sub17 = new SubEntity(17L, "핀즐", "자취방 꾸미고 싶은 사람들을 위한 그림 구독 서비스", categoryEntities.get(0), 26000L, 3, "pinzle.png");
        SubEntity sub18 = new SubEntity(18L, "꾸까", "2주마다 꽃 배달 서비스", categoryEntities.get(0), 30000L, 3, "kukka.png");
        SubEntity sub19 = new SubEntity(19L, "커피 리브레", "매주 다른 종류의 커피 배달", categoryEntities.get(3), 48000L, 5, "coffeelibre.jpeg");

        return Arrays.asList(sub1, sub2, sub3, sub4, sub5, sub6, sub7, sub8, sub9,
                sub10, sub11, sub12, sub13, sub14, sub15, sub16, sub17, sub18, sub19);
    }

    public static List<SpendingEntity> generateSpendingEntities(String[] userIds, String[] categories) {
        Random random = new Random();
        List<SpendingEntity> spendingEntities = new ArrayList<>();

        for (String userId : userIds) {
            for (int i = 0; i < 50; i++) {
                SpendingEntity spendingEntity = new SpendingEntity();
                spendingEntity.setUserId(userId);
                spendingEntity.setCategory(categories[random.nextInt(categories.length)]);
                spendingEntity.setAmount(random.nextLong(1000, 100000));
                spendingEntities.add(spendingEntity);
            }
        }

        return spendingEntities;
    }
}