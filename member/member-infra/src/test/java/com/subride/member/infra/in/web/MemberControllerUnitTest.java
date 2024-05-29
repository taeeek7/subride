package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.MemberInfoDTO;
import com.subride.member.infra.exception.InfraException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/*
단위 테스트 예시: Controller 테스트
주요 핵심
- 목적:
*/
@ExtendWith(MockitoExtension.class)
class MemberControllerUnitTest {

    @Mock
    private MemberControllerHelper memberControllerHelper;

    @InjectMocks
    private MemberController memberController;

    /* -- JUnit5의 새로운 기능인 @ExtendWith(MockitoExtension.class)을 사용하면 아래 강제 주입은 하면 안됨
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     */

    //-- getMemberInfo 동작 테스트
    @Test
    void getMemberInfo_ValidUserId_ReturnsMemberInfoDTO() {
        // given
        String userId = "testUser";
        MemberInfoDTO expectedMemberInfo = new MemberInfoDTO();
        expectedMemberInfo.setUserId(userId);

        given(memberControllerHelper.getMemberInfo(userId)).willReturn(expectedMemberInfo);

        // when
        ResponseEntity<ResponseDTO<MemberInfoDTO>> response = memberController.getMemberInfo(userId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseDTO<MemberInfoDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedMemberInfo, responseBody.getResponse());

        verify(memberControllerHelper, times(1)).getMemberInfo(userId);
    }

    //-- 에러 처리 테스트
    @Test
    void getMemberInfo_InvalidUserId_ThrowsInfraException() {
        // given
        String userId = "invalidUser";

        given(memberControllerHelper.getMemberInfo(userId)).willThrow(new InfraException("User not found"));

        // when
        ResponseEntity<ResponseDTO<MemberInfoDTO>> response = memberController.getMemberInfo(userId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseDTO<MemberInfoDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNull(responseBody.getResponse());

        verify(memberControllerHelper, times(1)).getMemberInfo(userId);
    }

    // Add more test cases for other methods and scenarios
}