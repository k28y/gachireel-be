package com.gachireel.api.configuration.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final JwtMember jwtMember;

    // 로그인 유저 아이디 반환
    public long getLoginUserId() { return jwtMember.getLoginUserId(); }

    // 로그인 유저 구분/권한 반환
    public String getLoginUserRole() { return jwtMember.getLoginUserRole(); }

    @Override // Spring Security가 유저의 권한을 물어볼때 호출하는 메소드. Collection: 한 유저가 여러개 권한 가질수 있음
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 객체 생성
        return List.of( new SimpleGrantedAuthority( "ROLE_" + jwtMember.getLoginUserRole() ) );
    }

    @Override
    public String getPassword() { return ""; }

    @Override
    public String getUsername() { return ""; }
}