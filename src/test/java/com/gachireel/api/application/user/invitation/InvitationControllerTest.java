package com.gachireel.api.application.user.invitation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachireel.api.application.user.invitation.model.InviteReq;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationService invitationService;

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
    @DisplayName("초대 링크 발송 API")
    void createInvitation() throws Exception {
        InviteReq req = new InviteReq("friend@example.com");

        mockMvc.perform(post("/api/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("초대 링크를 발송했습니다."))
                .andDo(print())
                .andDo(MockMvcRestDocumentationWrapper.document("invitation-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Invitation")
                                .summary("초대 링크 발송")
                                .description("입력한 이메일로 초대 링크를 발송합니다. 링크는 7일간 유효합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("초대할 상대방의 이메일")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                                )
                                .build()
                        )
                ));
    }
}