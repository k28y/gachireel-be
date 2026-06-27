package com.gachireel.api.common.scheduler;

import com.gachireel.api.application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeepAliveScheduler {

    private final UserRepository userRepository;

    // 5일마다 실행
    @Scheduled(fixedRate = 1000L * 60 * 60 * 24 * 5)
    public void keepAlive() {
        long count = userRepository.count();
        log.info("[KeepAlive] Supabase DB ping 성공 - users count: {}", count);
    }
}