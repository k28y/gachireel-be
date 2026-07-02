package com.gachireel.api.application.storage;

import com.gachireel.api.common.exception.AppException;
import com.gachireel.api.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileStorageClient {

    private final RestClient restClient;

    @Value("${app.supabase-url}")
    private String supabaseUrl;

    @Value("${app.supabase-service-key}")
    private String serviceKey;

    private static final String BUCKET = "profile-images";

    public FileStorageClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // 프로필 이미지 업로드 (덮어쓰기) → 공개 URL 반환
    public String upload(Long userId, byte[] bytes, String contentType) {
        String path = "profiles/" + userId;
        try {
            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + BUCKET + "/" + path)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("파일 업로드 실패 (userId={}): {}", userId, e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return supabaseUrl + "/storage/v1/object/public/" + BUCKET + "/" + path;
    }

    // 프로필 이미지 삭제
    public void delete(Long userId) {
        String path = "profiles/" + userId;
        try {
            restClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(supabaseUrl + "/storage/v1/object/" + BUCKET)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", List.of(path)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("파일 삭제 실패 (userId={}): {}", userId, e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}