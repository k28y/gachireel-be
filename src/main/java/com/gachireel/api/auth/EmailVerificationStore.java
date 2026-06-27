package com.gachireel.api.auth;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailVerificationStore {

    private record Entry(String code, LocalDateTime expiresAt) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    // 인증코드 저장 (5분 유효)
    public void save(String email, String code) {
        store.put(email, new Entry(code, LocalDateTime.now().plusMinutes(5)));
    }

    // 인증코드 검증 (만료 시 자동 삭제)
    public boolean verify(String email, String code) {
        Entry entry = store.get(email);
        if (entry == null) return false;
        if (entry.expiresAt().isBefore(LocalDateTime.now())) {
            store.remove(email);
            return false;
        }
        return entry.code().equals(code);
    }

    // 사용 후 삭제
    public void remove(String email) {
        store.remove(email);
    }
}