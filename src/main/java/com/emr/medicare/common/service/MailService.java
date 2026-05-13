package com.emr.medicare.common.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetMail(
            String to,
            String token
    ) {

        String resetLink =
                "http://localhost:3000/reset-password?token="
                        + token;

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(to);

            helper.setSubject(
                    "[Medicare] 비밀번호 재설정"
            );

            helper.setFrom(
                    fromEmail,
                    "Medicare 인증센터"
            );

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
            throw new RuntimeException(
                    "메일 전송 실패"
            );
        }
    }
}