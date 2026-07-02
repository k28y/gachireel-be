package com.gachireel.api.application.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender mailSender;

    public void sendVerificationCodeMail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("[가치릴] 비밀번호 재설정 인증코드");
        helper.setText(getVerificationCodeTemplate(code), true);
        mailSender.send(message);
    }

    private String getVerificationCodeTemplate(String code) {
        String body = """
                <p style="color:#333;font-size:16px;line-height:1.6;margin:0 0 16px 0;">
                  비밀번호 재설정 인증코드입니다.<br>
                  아래 코드를 입력해 주세요. (5분 이내 사용)
                </p>
                <div style="background:#f8f8f8;border-radius:8px;padding:20px;text-align:center;">
                  <span style="font-size:36px;font-weight:bold;letter-spacing:12px;color:#1a1a1a;">%s</span>
                </div>
                """.formatted(code);
        return wrapTemplate(body, "본인이 요청하지 않은 경우 이 메일을 무시하세요.");
    }

    public void sendApprovalMail(String to, String frontendUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("[가치릴] 가입이 승인됐습니다");
        helper.setText(getApprovalTemplate(frontendUrl), true);
        mailSender.send(message);
    }

    private String getApprovalTemplate(String frontendUrl) {
        String body = """
                <p style="color:#333;font-size:16px;line-height:1.6;margin:0;">
                  가입이 승인됐습니다.<br><br>
                  이제 가치릴에서 영화·드라마 리뷰를<br>
                  함께 나눠보세요.
                </p>
                """ + buttonHtml(frontendUrl, "로그인하기");
        return wrapTemplate(body, "본인이 가입 신청한 적이 없다면 이 메일을 무시하세요.");
    }

    public void sendRejectionMail(String to) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("[가치릴] 가입 신청 결과 안내");
        helper.setText(getRejectionTemplate(), true);
        mailSender.send(message);
    }

    private String getRejectionTemplate() {
        String body = """
                <p style="color:#333;font-size:16px;line-height:1.6;margin:0;">
                  가입 신청을 검토한 결과,<br>
                  이번에는 승인이 어렵게 됐습니다.<br><br>
                  문의 사항은 초대해 주신 분께 연락해 주세요.
                </p>
                """;
        return wrapTemplate(body, "본인이 가입 신청한 적이 없다면 이 메일을 무시하세요.");
    }

    public void sendInvitationMail(String to, String inviteUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("[가치릴] 초대장이 도착했습니다");
        helper.setText(getInvitationTemplate(inviteUrl), true);
        mailSender.send(message);
    }

    private String getInvitationTemplate(String inviteUrl) {
        String body = """
                <p style="color:#333;font-size:16px;line-height:1.6;margin:0;">
                  가치릴에 초대됐습니다.<br>
                  아래 버튼을 눌러 가입 신청을 완료하세요.
                </p>
                """ + buttonHtml(inviteUrl, "가입하기");
        return wrapTemplate(body, "이 링크는 7일 후 만료됩니다.<br>본인이 요청하지 않은 경우 이 메일을 무시하세요.");
    }

    // 모든 메일의 공통 레이아웃 (헤더 + 본문 영역 + footer)
    private String wrapTemplate(String mainContent, String footerNote) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                  <div style="padding:40px 16px;">
                    <div style="max-width:500px;margin:0 auto;background:#fff;border-radius:12px;padding:40px;">
                      <div style="text-align:center;padding-bottom:24px;">
                        <h1 style="color:#1a1a1a;font-size:24px;margin:0;">가치릴</h1>
                        <p style="color:#666;font-size:14px;margin:8px 0 0;">영화·드라마 리뷰 커뮤니티</p>
                      </div>
                      <div style="padding:24px 0;border-top:1px solid #eee;border-bottom:1px solid #eee;">
                        %s
                      </div>
                      <div style="color:#999;font-size:12px;text-align:center;padding-top:24px;">
                        %s
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(mainContent, footerNote);
    }

    // 버튼이 필요한 메일에서 사용
    private String buttonHtml(String url, String label) {
        return """
                <div style="text-align:center;padding-top:28px;">
                  <a href="%s" style="background:#1a1a1a;color:#fff;text-decoration:none;padding:14px 36px;border-radius:8px;font-size:16px;font-weight:bold;">%s</a>
                </div>
                """.formatted(url, label);
    }
}