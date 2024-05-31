// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/util/TestDataGenerator.java
package com.subride.mysub.infra.common.util;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.infra.out.entity.MySubEntity;

import java.util.ArrayList;
import java.util.List;

public class TestDataGenerator {
    public static final String testUserId = "user01";
    public static final Long testSubId = 1L;
    public static final Long testGroupId = 1L;

    public static List<MySubEntity> generateMySubEntities() {
        List<MySubEntity> mySubEntities = new ArrayList<>();

        MySubEntity mySubEntity = new MySubEntity();
        mySubEntity.setUserId(testUserId);
        mySubEntity.setSubId(testSubId);
        mySubEntities.add(mySubEntity);
        return mySubEntities;
    }

    public static GroupSummaryDTO generateGroupSumaryDTO() {
        GroupSummaryDTO group = new GroupSummaryDTO();
        group.setGroupId(testGroupId);
        group.setGroupName("썹그룹1");
        group.setSubId(testSubId);
        group.setPaymentDay(7);
        group.setSubName("넷플릭스");
        group.setLogo("abc.png");
        group.setFee(15000L);
        group.setMemberCount(2);
        return group;
    }

    public static SubInfoDTO generateSubInfoDTO() {
        SubInfoDTO sub = new SubInfoDTO();
        sub.setSubId(testSubId);
        sub.setCategoryName("생필품");
        sub.setSubName("넷플릭스");
        sub.setDescription("온세상 미디어");
        sub.setFee(15000L);
        sub.setLogo("netflix.png");
        sub.setMaxShareNum(5);

        return sub;
    }

    public static <T> ResponseDTO<T> generateResponseDTO(int code, T response) {
        return ResponseDTO.<T>builder()
                .code(code)
                .response(response)
                .build();
    }
}