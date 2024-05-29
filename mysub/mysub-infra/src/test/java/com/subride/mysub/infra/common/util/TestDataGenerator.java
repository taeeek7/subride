// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/util/TestDataGenerator.java
package com.subride.mysub.infra.common.util;

import com.subride.mysub.infra.out.entity.MySubEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {
    public static List<MySubEntity> generateMySubEntities(String userId) {
        List<MySubEntity> mySubEntities = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            MySubEntity mySubEntity = new MySubEntity();
            mySubEntity.setUserId(userId);
            mySubEntity.setSubId(random.nextLong(1, 100));
            mySubEntities.add(mySubEntity);
        }

        return mySubEntities;
    }
}