package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;

public class CommonTestUtils {

    public static Category createCategory() {
        Category category = new Category();
        category.setCategoryId("food");
        category.setCategoryName("음식");
        category.setSpendingCategory("Food");
        return category;
    }

    public static Sub createSub() {
        Category category = createCategory();

        Sub sub = new Sub();
        sub.setId(1L);
        sub.setCategory(category);
        sub.setName("넷플릭스");
        sub.setDescription("온세상 미디어");
        sub.setFee(15000L);
        sub.setLogo("netflix.png");
        sub.setMaxShareNum(5);

        return sub;
    }

}
