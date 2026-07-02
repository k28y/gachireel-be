package com.gachireel.api.configuration.security;

import com.gachireel.api.application.auth.entity.RefreshToken;
import com.gachireel.api.application.auth.repository.RefreshTokenRepository;
import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import com.gachireel.api.configuration.constants.ConstJwt;
import com.gachireel.api.configuration.model.JwtMember;
import com.gachireel.api.configuration.model.UserPrincipal;
import com.gachireel.api.configuration.util.MyCookieUtil;
import com.gachireel.api.application.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenManager {
    private final ConstJwt constJwt;
    private final MyCookieUtil myCookieUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인 시 토큰 발급 + RT DB저장 + 쿠키 설정
    @Transactional
    public void issue(HttpServletResponse res, User user) {
        JwtMember jwtMember = new JwtMember(user.getId(), user.getRole().name());
        String accessToken = jwtTokenProvider.generateAccessToken(jwtMember);
        String refreshToken = jwtTokenProvider.generateRefreshToken(jwtMember);

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(constJwt.getRefreshTokenValidityMilliseconds(), ChronoUnit.MILLIS))
                .build());

        setAccessTokenInCookie(res, accessToken);
        setRefreshTokenInCookie(res, refreshToken);
    }

    // AT를 새롭게 생성후 쿠키에 담기
    public void setAccessTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String accessToken = jwtTokenProvider.generateAccessToken(jwtMember); // 토큰 생성
        setAccessTokenInCookie(res, accessToken); // 만들어진 AT를 쿠키에 담는 메소드 호출
    }
    // RT를 새롭게 생성후 쿠키에 담기
    public void setRefreshTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String refreshToken = jwtTokenProvider.generateRefreshToken(jwtMember);
        setRefreshTokenInCookie(res, refreshToken); // 만들어진 RT를 쿠키에 담는 메소드 호출
    }

    // 만들어진 AT를 쿠키에 담기
    public void setAccessTokenInCookie(HttpServletResponse res, String accessToken){
        myCookieUtil.setCookie(res,
                constJwt.getAccessTokenCookieName(),
                accessToken,
                constJwt.getAccessTokenCookieValiditySeconds(),
                constJwt.getAccessTokenCookiePath()
        );
    }

    // 만들어진 RT를 쿠키에 담기
    public void setRefreshTokenInCookie(HttpServletResponse res, String refreshToken){
        myCookieUtil.setCookie(res,
                constJwt.getRefreshTokenCookieName(),
                refreshToken,
                constJwt.getRefreshTokenCookieValiditySeconds(),
                constJwt.getRefreshTokenCookiePath()
        );
    }

    // 쿠키에서 AT꺼내기
    public String getAccessTokenFromCookie(HttpServletRequest req){
        return myCookieUtil.getValue(req, constJwt.getAccessTokenCookieName());
    }

    // 쿠키에서 RT꺼내기
    public String getRefreshTokenFromCookie(HttpServletRequest req) {
        return myCookieUtil.getValue(req, constJwt.getRefreshTokenCookieName());
    }

    // 쿠키에 든 AT를 확인
    public Authentication getAuthentication(HttpServletRequest req){
        String accessToken = getAccessTokenFromCookie(req); // req의 쿠키 속 AT 꺼내기

        // 쿠키에 AT 없다면(= 로그인 안된 상태), null 반환
        if(accessToken == null){ return null; }

        //쿠키에 AT가 있다면, JWT에 담았던 JwtMember객체(로그인 유저 정보)를 빼내어, 시큐리티를 위한 UserPrincipal로 변환
        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(accessToken);
        UserPrincipal userPrincipal = new UserPrincipal(jwtMember);

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        // 시큐리티 인증된 로그인 유저 정보(로그인ID, role 담아 반환. @AuthenticationPrincipal로 활용)
    }

    // AT 삭제
    public void deleteAccessTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.getAccessTokenCookieName(), constJwt.getAccessTokenCookiePath());
    }

    // RT 삭제
    public void deleteRefreshTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.getRefreshTokenCookieName(), constJwt.getRefreshTokenCookiePath());
    }

    // 로그아웃할 때 AT, RT 삭제
    @Transactional
    public void logOut(HttpServletRequest req, HttpServletResponse res){
        String refreshToken = getRefreshTokenFromCookie(req);
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
        // DB 삭제 성공 여부와 관계없이 쿠키는 항상 삭제
        deleteAccessTokenInCookie(res);
        deleteRefreshTokenInCookie(res);
    }

    // 유효한 AT가 쿠키에 있는지 확인
    public boolean hasValidAccessToken(HttpServletRequest req) {
        String accessToken = getAccessTokenFromCookie(req);
        if (accessToken == null) return false;
        try {
            jwtTokenProvider.getJwtMemberFromToken(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // AT 재발행 (RT DB 검증 포함)
    @Transactional
    public void reissue(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = getRefreshTokenFromCookie(req);
        if (refreshToken == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new AppException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(refreshToken);
        setAccessTokenInCookie(res, jwtMember);
    }
}