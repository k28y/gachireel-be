package com.gachireel.api.auth;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachireel.api.application.auth.AuthController;
import com.gachireel.api.application.auth.AuthService;
import com.gachireel.api.application.auth.model.*;
import com.gachireel.api.application.user.entity.User;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.configuration.security.JwtTokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenManager jwtTokenManager;

    // @EnableJpaAuditing이 ApiApplication에 있어 @WebMvcTest에서 JPA 컨텍스트 충돌 방지
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("가입 신청 API")
    void register() throws Exception {
        RegisterReq req = new RegisterReq("uuid-token-example", "password123", "테스터");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입 신청이 완료됐습니다. 관리자 승인 후 로그인 가능합니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-register",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("가입 신청")
                                .description("초대 토큰과 비밀번호, 닉네임으로 가입 신청합니다. 관리자 승인 후 로그인 가능합니다.")
                                .requestFields(
                                        fieldWithPath("token").type(JsonFieldType.STRING).description("초대 링크의 UUID 토큰"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (8자 이상)"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임 (30자 이하)")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("로그인 API")
    void login() throws Exception {
        User mockUser = User.builder()
                .email("user@example.com")
                .password("encoded_password")
                .nickname("관리자")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        when(authService.login(any())).thenReturn(mockUser);

        LoginReq req = new LoginReq("user@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-login",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("로그인")
                                .description("이메일과 비밀번호로 로그인합니다. 성공 시 AT/RT가 HttpOnly 쿠키로 설정됩니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                        fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("result.role").type(JsonFieldType.STRING).description("권한 (USER / ADMIN)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("로그아웃 API")
    void logout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-logout",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("로그아웃")
                                .description("AT/RT 쿠키를 삭제하고 DB의 Refresh Token을 제거합니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("AT 재발급 API")
    void reissue() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access Token 재발행"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-reissue",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("AT 재발급")
                                .description("RT 쿠키로 만료된 AT를 재발급합니다. RT는 DB 검증 후 새 AT 쿠키가 설정됩니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 재설정 인증코드 발송 API")
    void sendVerificationCode() throws Exception {
        doNothing().when(authService).sendVerificationCode(any());

        PasswordEmailReq req = new PasswordEmailReq("user@example.com");

        mockMvc.perform(post("/api/auth/password/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드를 발송했습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-password-email",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("비밀번호 재설정 인증코드 발송")
                                .description("입력한 이메일로 6자리 인증코드를 발송합니다. 코드는 5분간 유효합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("가입된 이메일")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 재설정 API")
    void resetPassword() throws Exception {
        PasswordResetReq req = new PasswordResetReq("user@example.com", "123456", "newpassword123");

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경됐습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("auth-password-reset",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("비밀번호 재설정")
                                .description("이메일로 받은 인증코드와 새 비밀번호를 함께 제출해 비밀번호를 변경합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("가입된 이메일"),
                                        fieldWithPath("code").type(JsonFieldType.STRING).description("이메일로 받은 6자리 인증코드"),
                                        fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호 (8자 이상)")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }
}