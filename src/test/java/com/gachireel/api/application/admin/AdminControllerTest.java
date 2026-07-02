package com.gachireel.api.application.admin;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.gachireel.api.application.admin.model.GetAdminUserRes;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtTokenManager jwtTokenManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("유저 목록 조회 API")
    void getUserList() throws Exception {
        GetAdminUserRes mockUser = new GetAdminUserRes(
                1L, "user@example.com", "테스터",
                UserStatus.PENDING, UserRole.USER,
                LocalDateTime.of(2026, 7, 1, 12, 0), null, null, "초대자"
        );
        when(adminService.getUserList(any())).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/api/admin/users")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 목록 조회 성공"))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("admin-users-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin")
                                .summary("유저 목록 조회")
                                .description("전체 유저 목록을 최신순으로 조회합니다. status 파라미터로 필터링할 수 있습니다.")
                                .queryParameters(
                                        parameterWithName("status").optional().description("상태 필터 (PENDING / ACTIVE / REJECTED / DELETED). 생략 시 전체 조회")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                        fieldWithPath("result[].id").type(JsonFieldType.NUMBER).description("유저 ID"),
                                        fieldWithPath("result[].email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("result[].nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("result[].status").type(JsonFieldType.STRING).description("상태 (PENDING / ACTIVE / REJECTED / DELETED)"),
                                        fieldWithPath("result[].role").type(JsonFieldType.STRING).description("권한 (USER / ADMIN)"),
                                        fieldWithPath("result[].createdAt").type(JsonFieldType.STRING).description("가입 신청일"),
                                        fieldWithPath("result[].approvedAt").type(JsonFieldType.STRING).optional().description("승인일"),
                                        fieldWithPath("result[].deletedAt").type(JsonFieldType.STRING).optional().description("탈퇴일"),
                                        fieldWithPath("result[].referredByNickname").type(JsonFieldType.STRING).optional().description("초대한 유저 닉네임")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("가입 승인 API")
    void approveUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/{id}/approve", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입을 승인했습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("admin-approve",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin")
                                .summary("가입 승인")
                                .description("PENDING 상태의 유저를 ACTIVE로 변경합니다. Admin 권한 필요.")
                                .pathParameters(
                                        parameterWithName("id").description("승인할 유저 ID")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("거절 취소 API")
    void revertRejection() throws Exception {
        doNothing().when(adminService).revertRejection(any());

        mockMvc.perform(post("/api/admin/users/{id}/revert", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("거절을 취소했습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("admin-revert",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin")
                                .summary("거절 취소")
                                .description("REJECTED 상태의 유저를 PENDING으로 되돌립니다. Admin 권한 필요.")
                                .pathParameters(
                                        parameterWithName("id").description("거절 취소할 유저 ID")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("가입 거절 API")
    void rejectUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/{id}/reject", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입을 거절했습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("admin-reject",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Admin")
                                .summary("가입 거절")
                                .description("PENDING 상태의 유저를 REJECTED로 변경합니다. Admin 권한 필요.")
                                .pathParameters(
                                        parameterWithName("id").description("거절할 유저 ID")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }
}