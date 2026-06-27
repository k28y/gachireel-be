package com.gachireel.api.auth;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachireel.api.application.auth.AuthController;
import com.gachireel.api.application.auth.AuthService;
import com.gachireel.api.application.auth.model.LoginReq;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.common.enumcode.UserStatus;
import com.gachireel.api.configuration.security.JwtTokenManager;
import com.gachireel.api.application.user.entity.User;
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
}