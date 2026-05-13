package com.emr.medicare.common.service;

import com.emr.medicare.common.exception.BaseException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend-base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    /** auto | smtp | console — application.yml 의 app.mail.delivery */
    @Value("${app.mail.delivery:auto}")
    private String mailDelivery;

    public void sendPasswordResetMail(String to, String token) {
        String base = frontendBaseUrl.trim().replaceAll("/+$", "");
        String resetLink =
                base + "/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String mode = mailDelivery == null ? "auto" : mailDelivery.trim().toLowerCase();
        boolean logOnly =
                "console".equals(mode) || ("auto".equals(mode) && !StringUtils.hasText(fromEmail));

        if (logOnly) {
            log.warn(
                    "[비밀번호 재설정] SMTP 미사용 — 아래 링크로 직접 재설정하세요. 수신 이메일: {}\n{}",
                    to,
                    resetLink
            );
            log.warn(
                    "실제로 메일을 발송하려면 .env 에 MAIL_USERNAME, MAIL_PASSWORD(Gmail 앱 비밀번호)를 넣거나 APP_MAIL_DELIVERY=smtp 로 설정하세요."
            );
            return;
        }

        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalStateException(
                    "spring.mail.username 이 비어 있습니다. 메일 발송을 쓰려면 MAIL_USERNAME 을 설정하세요."
            );
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Medicare] 비밀번호 재설정");
            helper.setFrom(fromEmail, "Medicare 인증센터");
            helper.setText(
                    """
                    <div>
                        <h2>비밀번호 재설정</h2>
                        <p>
                            아래 버튼을 클릭하여
                            비밀번호를 재설정하세요.
                        </p>
                        <a href="%s">
                            비밀번호 재설정
                        </a>
                        <p>
                            링크는 10분간 유효합니다.
                        </p>
                    </div>
                    """.formatted(resetLink),
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패 — to={}, from={}", to, fromEmail, e);
            throw new BaseException(
                    HttpStatus.BAD_GATEWAY,
                    "메일 전송 실패: " + e.getMessage()
            );
        }
    }
}