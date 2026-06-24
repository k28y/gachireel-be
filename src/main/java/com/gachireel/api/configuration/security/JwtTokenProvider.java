package com.gachireel.api.configuration.security;

import com.gachireel.api.configuration.constants.ConstJwt;
import com.gachireel.api.configuration.model.JwtMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final ObjectMapper objectMapper;
    private final ConstJwt constJwt;
    private final SecretKey secretKey;

    public JwtTokenProvider(ObjectMapper objectMapper, ConstJwt constJwt) {
        this.objectMapper = objectMapper;
        this.constJwt = constJwt;
        // 시크릿 키 문자열을 암호화에 쓸 수 있는 키 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(constJwt.getSecretKey()));

        log.info("constJwt: {}", this.constJwt);
    }

    // 15분 만료 토큰 생성
    public String generateAccessToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.getAccessTokenValidityMilliseconds());
    }

    // 15일 만료 토큰 생성
    public String generateRefreshToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.getRefreshTokenValidityMilliseconds());
    }

    // JWT 문자열 만드는 메소드. 암호화된 문자열(데이터, 토큰만료 시간 포함)
    public String generateToken(JwtMember jwtMember, long tokenValidityMilleSeconds){
        Date now = new Date();
        return Jwts.builder()
                // header
                .header().type(constJwt.getBearerFormat())
                .and()
                //payload
                .issuer(constJwt.getIssuer()) // 발급자
                .issuedAt(now) // 토큰 발급 시간
                .expiration(new Date(now.getTime() + tokenValidityMilleSeconds)) // 토큰 만료 시간
                .claim(constJwt.getClaimKey(), makeClaimMyUserToJson(jwtMember)) // 로그인 유저 실제 데이터
                /// key(loginMember) , value(JwtMember객체)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 안에 jwtMember 데이터 넣기
    public String makeClaimMyUserToJson(JwtMember jwtMember) {
        try {
            return objectMapper.writeValueAsString(jwtMember);
        } catch (Exception e) {
            throw new RuntimeException("JwtMember 직렬화 실패", e);
        }
    }

    // 토큰 안에 jwtMember 데이터 꺼내기
    public JwtMember getJwtMemberFromToken(String token) {
        Claims claims = getClaims(token);
        String json = claims.get(constJwt.getClaimKey(), String.class);
        try {
            return objectMapper.readValue(json, JwtMember.class);
        } catch (Exception e) {
            throw new RuntimeException("JwtMember 역직렬화 실패", e);
        }
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}