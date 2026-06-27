package com.gachireel.api.application.admin;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
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

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
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