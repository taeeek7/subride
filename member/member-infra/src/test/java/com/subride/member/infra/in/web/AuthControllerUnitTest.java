package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.JwtTokenRefreshDTO;
import com.subride.member.infra.common.dto.JwtTokenVarifyDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/*
단위 테스트 코드 예제
- 목적: Service레어어로 요청 처리와 결과 반환 처리를 검증
- Service레이어 객체는 모의객체를 사용
*/

//-- 단위 테스트에는 모의 객체 생성 및 관리를 편하게 하기 위해 MockitoExtension을 사용함
@ExtendWith(MockitoExtension.class)
/*
추가로 필요한 Bean객체 로딩
- Controller클래스에 Spring security가 적용되므로 SucurtyConfig를 Import하여 필요한 Bean객체를 로딩해야함
- JWT토큰 처리를 위해 JwtTokenProvider객체도 import해야 함
*/
public class AuthControllerUnitTest {
    //-- 필요한 모의 객체를 생성
    @Mock
    private IAuthService authService;

    //-------------------------------------

    //--- AuthControllerHelper에서 사용할 JwtTokenProvider 객체 생성
    private String jwtSecret = CommonTestUtils.getTestJwtSecret();
    private long jwtExpirationTime = 3600;
    private long jwtRefreshTokenExpirationTime = 36000;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private JwtTokenDTO jwtTokenDTO;
    //------------------

    //-- 모의 객체가 아닌 실제 객체를 생성하고, 실제 객체의 일부 메소드 결과를 대체하기 위해 Spy객체를 생성
    //-- Spy 대상 객체는 반드시 빈 생성자 메소드를 제공해야 함
    @Spy
    private AuthControllerHelper authControllerHelper;

    //-- 생성된 모의 객체와 Spy객체를 필요한 객체에 주입
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        /*
        Spy객체를 수동으로 생성하여 auController에 주입하는 예시임
        만약 Spy객체를 생성할 대상 객체가 빈 생성자를 제공하지 않는다면, 이 방법을 써야함
        이때는 위에서 @spy와 @InjectMocks 어노테이션을 제거해야 함
        */
        /*
        //-- 모의 객체가 아닌 실제 객체를 생성하고, 실제 객체의 일부 메소드 결과를 대체하기 위해 Mockito.spy를 이용
        authControllerHelper = Mockito.spy(new AuthControllerHelper(jwtTokenProvider, memberRepository, accountRepository));
        //-- authService는 모의 객체이고, authControllerHelper객체는 실제 객체를 감싼 Spy객체임
        authController = new AuthController(authService, authControllerHelper);
        */
        jwtTokenProvider = new JwtTokenProvider(jwtSecret, jwtExpirationTime, jwtRefreshTokenExpirationTime);
        authControllerHelper.setJwtTokenProvider(jwtTokenProvider);
        jwtTokenDTO = CommonTestUtils.createTestToken(jwtTokenProvider);
    }
    //-----------------------------

    /*
    authController.signup 호출 시 정상적으로 처리를 위한 메소드가 1번 호출되는지만 확인
    */
    @Test
    void signup_test_call() {
        //given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();

        Member member = new Member();
        Account account = new Account();

        /*
        Stub 처리: 일부 메소드를 실제 수행하지 않고 지정된 값을 리턴하도록 대체함
        Spy객체 authControllerHelper의 메소드 결과를 위에서 생성한 member와 account로 대체
        Spy객체를 Stub할 때는 doReturn().when()문을 써야 문제가 안 생김
         */
        doReturn(member).when(authControllerHelper).getMemberFromRequest(signupRequestDTO);
        doReturn(account).when(authControllerHelper).getAccountFromRequest(signupRequestDTO);

        /*
        모의 객체 authService의 signup메소드를 Stub함
        authService.signup의 결과가 void이기 때문에 willDoNothing을 사용
        모의 객체를 Stub할 때는 BDDMockito에서 권장하는 given을 사용해도 되고, Mockito에서 제공하는 when을 사용해도 되는데 given이 더 권장됨
         */
        willDoNothing().given(authService).signup(eq(member), eq(account));
        //doNothing().when(authService).signup(eq(member), eq(account));

        //when
        ResponseEntity<ResponseDTO<String>> response = authController.signup(signupRequestDTO);

        //then
        //-- 지정한 객체의 메소드가 지정한 횟수만큰 호출되었는지 검증
        verify(authControllerHelper, times(1)).getMemberFromRequest(any(SignupRequestDTO.class));
        verify(authControllerHelper, times(1)).getAccountFromRequest(any(SignupRequestDTO.class));
        verify(authService, times(1)).signup(any(Member.class), any(Account.class));
    }

    /*
    authController의 signup 메소드 테스트
    authController.signup 메소드 내에서 수행되는 아래 작업들을 테스트 함
    단, 이때 authService.signup은 Stub함

    authService.signup(authControllerHelper.getMemberFromRequest(signupRequestDTO),
                    authControllerHelper.getAccountFromRequest(signupRequestDTO));

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원가입 성공", "회원 가입 되었습니다."));
     */
    @Test
    void signup_ValidInput_ReturnSuccessResponse() {
        //given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();

        //-- authService.signup은 Stub하여 실제로는 수행되지 않게 함
        willDoNothing().given(authService).signup(any(Member.class), any(Account.class));

        //when
        ResponseEntity<ResponseDTO<String>> response = authController.signup(signupRequestDTO);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
        assertEquals("회원 가입 되었습니다.", response.getBody().getResponse());
    }

    /*
    login 단위 테스트 여부 결정
    - 대상: authService.login과 authControllerHelper.createToken
    - 결과: Database와 연결된 검증이 필요하여 단위테스트로 부적절
     */

    /*
    validate 단위 테스트
    - authControllerHelper.checkAccessToken 만 검증
    - authControllerHelper.getMemberFromToken은 DB연결이 필요하여 Stub함
     */
    @Test
    void validate_test_checkAccessToken() {
        //Given
        JwtTokenVarifyDTO jwtTokenVarifyDTO = new JwtTokenVarifyDTO();
        jwtTokenVarifyDTO.setToken(jwtTokenDTO.getAccessToken());

        Member member = new Member();
        member.setUserId("testuser");

        //-Stubbing
        doReturn(member).when(authControllerHelper).getMemberFromToken(jwtTokenVarifyDTO.getToken());
        given(authService.validateMemberAccess(member)).willReturn(true);

        //When
        /*
        주의사항: authConrollerHelper는 실제 실행되므로, 수행에 필요한 jwtTokenProvider도 실제 객체여야 함
        그래서, 위 setup()에서 authControllerHelper객체에 실제 생성한 jwtTokenProvider 객체를 셋팅하는 것임
         */
        ResponseEntity<ResponseDTO<Integer>> response = authController.validate(jwtTokenVarifyDTO);

        //Then
        assertEquals(200, response.getBody().getCode());
        assertEquals(1, response.getBody().getResponse());
    }

    /*
    refresh 테스트
    */
    @Test
    void refresh_test_isValidRefreshToken() {
        //Given
        JwtTokenRefreshDTO jwtTokenRefreshDTO = new JwtTokenRefreshDTO();
        jwtTokenRefreshDTO.setRefreshToken(jwtTokenDTO.getRefreshToken());

        Member member = new Member();
        doReturn(member).when(authControllerHelper).getMemberFromToken(any());
        doReturn(jwtTokenDTO).when(authControllerHelper).createToken(any());

        //When
        ResponseEntity<ResponseDTO<JwtTokenDTO>> response = authController.refresh(jwtTokenRefreshDTO);

        //Then
        assertEquals(200, response.getBody().getCode());
        assertEquals(jwtTokenDTO.getAccessToken(), response.getBody().getResponse().getAccessToken());
    }
}