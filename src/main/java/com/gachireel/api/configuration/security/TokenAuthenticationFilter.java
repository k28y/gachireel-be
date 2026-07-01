package com.gachireel.api.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// request, response는 무조건 filter를 거치니 그때 할 작업 진행
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter { // 요청 하나당 딱 한번만 실행되는 필터
    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("req-uri: {}", request.getRequestURI());

        try {
            // 쿠키에서 AT 꺼내 유효성 확인
            Authentication authentication = jwtTokenManager.getAuthentication(request);
            if (authentication != null) { // 쿠키에 AT 있다면
                // 인증 정보를 SecurityContext에 등록 → 컨트롤러에서 인증 정보 사용 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 쿠키에 AT 없다면 → SecurityContext 빈 상태 → Security가 401 처리
        } catch (Exception e) {
            // AT 만료, 손상의 경우 → SecurityContext 빈상태 → Security가 401 처리
            log.warn("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}