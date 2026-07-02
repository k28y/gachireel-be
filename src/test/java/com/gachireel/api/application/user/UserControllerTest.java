package com.gachireel.api.application.user;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachireel.api.application.user.model.*;
import com.gachireel.api.common.enumcode.UserRole;
import com.gachireel.api.configuration.model.JwtMember;
import com.gachireel.api.configuration.model.UserPrincipal;
import com.gachireel.api.configuration.security.JwtTokenManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenManager jwtTokenManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // @AuthenticationPrincipal 사용을 위해 SecurityContext에 로그인 유저 세팅
    @BeforeEach
    void setup() {
        UserPrincipal principal = new UserPrincipal(new JwtMember(1L, "USER"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("내 정보 조회 API")
    void getMyProfile() throws Exception {
        GetMyProfileRes mockRes = new GetMyProfileRes(
                1L, "user@example.com", "테스터", "안녕하세요", null,
                UserRole.USER, "스토리", "연출", null, null, null
        );
        when(userService.getMyProfile(anyLong())).thenReturn(mockRes);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 정보 조회 성공"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("내 정보 조회")
                                .description("로그인한 유저의 프로필 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                        fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("유저 ID"),
                                        fieldWithPath("result.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("result.bio").type(JsonFieldType.STRING).optional().description("자기소개"),
                                        fieldWithPath("result.pic").type(JsonFieldType.STRING).optional().description("프로필 이미지 URL"),
                                        fieldWithPath("result.role").type(JsonFieldType.STRING).description("권한 (USER / ADMIN)"),
                                        fieldWithPath("result.ratingCriteria1").type(JsonFieldType.STRING).optional().description("평점 기준 1"),
                                        fieldWithPath("result.ratingCriteria2").type(JsonFieldType.STRING).optional().description("평점 기준 2"),
                                        fieldWithPath("result.ratingCriteria3").type(JsonFieldType.STRING).optional().description("평점 기준 3"),
                                        fieldWithPath("result.ratingCriteria4").type(JsonFieldType.STRING).optional().description("평점 기준 4"),
                                        fieldWithPath("result.ratingCriteria5").type(JsonFieldType.STRING).optional().description("평점 기준 5")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("내 정보 수정 API")
    void updateProfile() throws Exception {
        doNothing().when(userService).updateProfile(anyLong(), any());

        UpdateProfileReq req = new UpdateProfileReq("새닉네임", "반갑습니다", "스토리", null, null, null, null);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 정보 수정 완료"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-patch",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("내 정보 수정")
                                .description("닉네임, 자기소개, 평점 기준을 선택적으로 수정합니다. null이면 변경하지 않고, 빈 문자열이면 초기화합니다.")
                                .requestFields(
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).optional().description("닉네임 (2~30자)"),
                                        fieldWithPath("bio").type(JsonFieldType.STRING).optional().description("자기소개 (200자 이하, 빈 문자열이면 초기화)"),
                                        fieldWithPath("ratingCriteria1").type(JsonFieldType.STRING).optional().description("평점 기준 1 (100자 이하)"),
                                        fieldWithPath("ratingCriteria2").type(JsonFieldType.STRING).optional().description("평점 기준 2"),
                                        fieldWithPath("ratingCriteria3").type(JsonFieldType.STRING).optional().description("평점 기준 3"),
                                        fieldWithPath("ratingCriteria4").type(JsonFieldType.STRING).optional().description("평점 기준 4"),
                                        fieldWithPath("ratingCriteria5").type(JsonFieldType.STRING).optional().description("평점 기준 5")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 API")
    void changePassword() throws Exception {
        doNothing().when(userService).changePassword(anyLong(), any());

        ChangePasswordReq req = new ChangePasswordReq("currentPass1!", "newPass1234!");

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경됐습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-password",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("비밀번호 변경")
                                .description("현재 비밀번호를 확인 후 새 비밀번호로 변경합니다.")
                                .requestFields(
                                        fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
                                        fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호 (8자 이상)")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 API")
    void deleteAccount() throws Exception {
        doNothing().when(userService).deleteAccount(anyLong());

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료됐습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("회원 탈퇴")
                                .description("소프트 딜리트로 처리됩니다. 이메일이 익명화되고 개인정보가 초기화됩니다. 관리자는 탈퇴할 수 없습니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("다른 유저 프로필 조회 API")
    void getUserProfile() throws Exception {
        GetUserProfileRes mockRes = new GetUserProfileRes(
                2L, "다른유저", "영화를 좋아합니다", null,
                "연출", "음악", null, null, null
        );
        when(userService.getUserProfile(anyLong())).thenReturn(mockRes);

        mockMvc.perform(get("/api/users/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 조회 성공"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-profile-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("다른 유저 프로필 조회")
                                .description("다른 유저의 공개 프로필을 조회합니다. 이메일, 권한 정보는 포함되지 않습니다.")
                                .pathParameters(
                                        parameterWithName("id").description("조회할 유저 ID")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                        fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("유저 ID"),
                                        fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("result.bio").type(JsonFieldType.STRING).optional().description("자기소개"),
                                        fieldWithPath("result.pic").type(JsonFieldType.STRING).optional().description("프로필 이미지 URL"),
                                        fieldWithPath("result.ratingCriteria1").type(JsonFieldType.STRING).optional().description("평점 기준 1"),
                                        fieldWithPath("result.ratingCriteria2").type(JsonFieldType.STRING).optional().description("평점 기준 2"),
                                        fieldWithPath("result.ratingCriteria3").type(JsonFieldType.STRING).optional().description("평점 기준 3"),
                                        fieldWithPath("result.ratingCriteria4").type(JsonFieldType.STRING).optional().description("평점 기준 4"),
                                        fieldWithPath("result.ratingCriteria5").type(JsonFieldType.STRING).optional().description("평점 기준 5")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로필 이미지 변경 API")
    void updateProfileImage() throws Exception {
        doNothing().when(userService).updateProfileImage(anyLong(), any());

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PATCH, "/api/users/me/profile-image")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 변경됐습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-profile-image-patch",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("프로필 이미지 변경")
                                .description("multipart/form-data로 이미지 파일을 업로드합니다. 이미지 파일만 허용됩니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 API")
    void deleteProfileImage() throws Exception {
        doNothing().when(userService).deleteProfileImage(anyLong());

        mockMvc.perform(delete("/api/users/me/profile-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 삭제됐습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("user-me-profile-image-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("프로필 이미지 삭제")
                                .description("프로필 이미지를 삭제하고 기본 이미지로 초기화합니다.")
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }
}