package com.gachireel.api.configuration.security;

import io.jsonwebtoken.MalformedJwtException;
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
// 쿠키안에 AT가 저장되어있는지 확인. 저장되어있으면 시큐리티 인증처리
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter { // 요청 하나당 딱 한번만 실행되는 필터
    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("req-uri: {}", request.getRequestURI());

        // 쿠키에 AT가 없다면 null 리턴, 쿠키에 AT가 있다면 주소값 리턴
        Authentication authentication = jwtTokenManager.getAuthentication(request); // 쿠키에서 AT 꺼내 유효한지 확인하고 누구인지 파악
        log.info("authentication: {}", authentication);
        try {
            if (authentication != null) {  //토큰이 유효하다면
                SecurityContextHolder.getContext().setAuthentication(authentication); //시큐리티 인증처리 완료 = 로그인 등록
            } else { // 토큰없거나, 유효하지 않은 토큰일시
                request.setAttribute("exception", new MalformedJwtException("토큰 확인")); // 예외를 토큰에 담아 다음 필터로 토스
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        //다음 필터에게 req, res 전달
        filterChain.doFilter(request, response);
    }
}