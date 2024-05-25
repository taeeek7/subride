package com.subride.member.biz.usecase.service;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.outport.IAuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/*
단위 테스트 예시: Service 테스트
*/
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    //-- IAuthProvider 모의 객체를 생성
    @Mock
    private IAuthProvider authProvider;

    /*
    - AuthServiceImpl 객체에 IAuthProvider 모의 객체 주입
    - AuthServiceImpl 객체 안에서 IAuthProvider를 호출하는 부분에서 실제 객체가 아닌 이 모의객체가 실행 됨
    */
    @InjectMocks
    private AuthServiceImpl authService;

    /* -- JUnit5의 새로운 기능인 @ExtendWith(MockitoExtension.class)을 사용하면 아래 강제 주입은 하면 안됨
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     */

    @Test
    void login_ValidCredentials_ReturnsMember() {
        // given
        String userId = "hiondal";
        String password = "0srilove$";
        Member expectedMember = new Member();
        expectedMember.setUserId(userId);

        //-- when안에 지정한 메소드가 호출될 때 그 결과값을 지정함. 아래에서는 위에서 만든 expectedMember객체가 리턴됨
        given(authProvider.validateAuth(userId, password)).willReturn(expectedMember);

        /*
         when: 테스트 할 login메소드를 호출하여 결과값을 받음
         - authService.login을 하면 authProvider.validateAuth메소드가 호출됨
         - 이때 바로 위의 when().thenReturn() 설정에 따라 expectedMember가 리턴됨
         */
        Member result = authService.login(userId, password);

        /*
         then: 결과를 검증함
         - assertEquals: authService를 실행한 결과값과 기대값이 동일한지 검증. 이 경우 login메소드에 문제가 없다면 당연히 되야 함
         - verify: authService.login 수행 시 authProvider.validateAuth함수가 1번만 호출되었는지 검증
         */
        assertEquals(expectedMember, result);
        verify(authProvider, times(1)).validateAuth(userId, password);
    }

    @Test
    void signup_ValidInput_CallsAuthProviderSignup() {
        // given
        Member member = new Member();
        Account account = new Account();
        member.setCharacterId((int) (Math.random() * 4) + 1);

        // when
        authService.signup(member, account);

        // then: 위 signup수행 시에 authProvider.signup함수가 1번만 호출되었는지 검증
        verify(authProvider, times(1)).signup(member, account);
    }

    // Add more test cases for edge cases and error scenarios
}