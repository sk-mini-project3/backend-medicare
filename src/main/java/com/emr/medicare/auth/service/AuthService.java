package com.emr.medicare.auth.service;

import com.emr.medicare.auth.dto.LoginRequest;
import com.emr.medicare.auth.dto.MeResponse;
import com.emr.medicare.auth.dto.ReissueRequest;
import com.emr.medicare.auth.dto.SignupRequest;
import com.emr.medicare.auth.dto.TokenResponse;
import com.emr.medicare.auth.dto.PasswordResetConfirmRequest;
import com.emr.medicare.auth.dto.PasswordResetRequest;
import com.emr.medicare.auth.entity.PasswordResetToken;
import com.emr.medicare.auth.repository.PasswordResetTokenRepository;
import com.emr.medicare.common.exception.TooManyRequestsException;
import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.common.service.MailService;
import com.emr.medicare.doctor.entity.DoctorDetail;
import com.emr.medicare.nurse.entity.NurseDetail;
import com.emr.medicare.security.jwt.JwtProvider;
import com.emr.medicare.user.entity.Role;
import com.emr.medicare.user.entity.User;
import com.emr.medicare.user.repository.UserRepository;
import com.emr.medicare.auth.entity.DoctorVerificationCode;
import com.emr.medicare.auth.entity.NurseVerificationCode;
import com.emr.medicare.auth.repository.DoctorVerificationCodeRepository;
import com.emr.medicare.auth.repository.NurseVerificationCodeRepository;
import com.emr.medicare.doctor.repository.DoctorDetailRepository;
import com.emr.medicare.nurse.repository.NurseDetailRepository;
import com.emr.medicare.patient.entity.PatientDetails;
import com.emr.medicare.patient.repository.PatientDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DoctorVerificationCodeRepository doctorVerificationCodeRepository;
    private final NurseVerificationCodeRepository nurseVerificationCodeRepository;
    private final DoctorDetailRepository doctorDetailRepository;
    private final NurseDetailRepository nurseDetailRepository;
    private final PatientDetailsRepository patientDetailsRepository;
    private final MailService mailService;

    @Transactional(readOnly = true)
    public MeResponse getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BaseException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new MeResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone() != null ? user.getPhone() : ""
        );
    }

    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 의사 검증
        if (request.getRole() == Role.DOCTOR) {

            DoctorVerificationCode verificationCode =
                    doctorVerificationCodeRepository
                            .findByDoctorCode(
                                    request.getDoctorCode()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "존재하지 않는 의사 인증코드입니다."
                                    )
                            );

            // 이름 검증 (공백 무시)
            String registeredName = verificationCode.getOwnerName() == null
                    ? ""
                    : verificationCode.getOwnerName().trim();
            String givenName = request.getName() == null ? "" : request.getName().trim();
            if (!registeredName.equals(givenName)) {
                throw new IllegalArgumentException(
                        "이름과 의사 인증코드가 일치하지 않습니다. 해당 코드에 등록된 이름은 「"
                                + registeredName
                                + "」입니다."
                );
            }

            // 이미 사용
            if (verificationCode.isUsed()) {

                throw new IllegalArgumentException(
                        "이미 사용된 의사 인증코드입니다."
                );
            }

            verificationCode.setUsed(true);

            doctorVerificationCodeRepository.save(
                    verificationCode
            );
        }

        // 간호사 검증
        if (request.getRole() == Role.NURSE) {

            NurseVerificationCode verificationCode =
                    nurseVerificationCodeRepository
                            .findByNurseCode(
                                    request.getNurseCode()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "존재하지 않는 간호사 인증코드입니다."
                                    )
                            );

            String registeredName = verificationCode.getOwnerName() == null
                    ? ""
                    : verificationCode.getOwnerName().trim();
            String givenName = request.getName() == null ? "" : request.getName().trim();
            if (!registeredName.equals(givenName)) {
                throw new IllegalArgumentException(
                        "이름과 간호사 인증코드가 일치하지 않습니다. 해당 코드에 등록된 이름은 「"
                                + registeredName
                                + "」입니다."
                );
            }

            // 이미 사용
            if (verificationCode.isUsed()) {

                throw new IllegalArgumentException(
                        "이미 사용된 간호사 인증코드입니다."
                );
            }

            verificationCode.setUsed(true);

            nurseVerificationCodeRepository.save(
                    verificationCode
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        userRepository.save(user);

        if (request.getRole() == Role.PATIENT && !patientDetailsRepository.existsById(user.getUserId())) {
            PatientDetails patientDetails = PatientDetails.builder()
                    .userId(user.getUserId())
                    .residentNumber("SIGNUP_PENDING")
                    .gender("미입력")
                    .birthDate(null)
                    .emergencyContact(null)
                    .bloodType(trimToNull(request.getBloodType()))
                    .address(null)
                    .insuranceInfo(trimToNull(request.getInsuranceInfo()))
                    .allergies(trimToNull(request.getAllergies()))
                    .build();
            patientDetailsRepository.save(patientDetails);
        }

        if (request.getRole() == Role.DOCTOR) {

            DoctorDetail doctorDetail =
                    DoctorDetail.builder()
                            .user(user)
                            .doctorCode(
                                    request.getDoctorCode()
                            )
                            .build();

            doctorDetailRepository.save(
                    doctorDetail
            );
        }

        if (request.getRole() == Role.NURSE) {

            NurseDetail nurseDetail =
                    NurseDetail.builder()
                            .user(user)
                            .nurseCode(
                                    request.getNurseCode()
                            )
                            .build();

            nurseDetailRepository.save(
                    nurseDetail
            );
        }
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {

        String failKey =
                "login:fail:" + request.getEmail();

        // 1. 로그인 잠금 여부 검사
        String failCount =
                redisTemplate.opsForValue().get(failKey);

        if (failCount != null &&
                Integer.parseInt(failCount) >= 5) {

            throw new TooManyRequestsException(
                    "로그인 5회 실패로 5분간 잠금되었습니다."
            );
        }

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElse(null);

        // 2. 이메일 없음
        if (user == null) {

            increaseFailCount(failKey);

            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. 비밀번호 틀림
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            increaseFailCount(failKey);

            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 4. 로그인 성공 시 실패 횟수 초기화
        redisTemplate.delete(failKey);

        String accessToken =
                jwtProvider.createAccessToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        String refreshToken =
                jwtProvider.createRefreshToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        redisTemplate.opsForValue().set(
                "refresh:" + user.getUserId(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getRole().name()
        );
    }

    public TokenResponse reissue(
            ReissueRequest request
    ) {

        String refreshToken =
                request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {

            throw new IllegalArgumentException(
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        Long userId =
                jwtProvider.getUserId(refreshToken);

        String savedRefreshToken =
                redisTemplate.opsForValue().get(
                        "refresh:" + userId
                );

        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new IllegalArgumentException(
                    "Refresh Token이 일치하지 않습니다."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        String newAccessToken =
                jwtProvider.createAccessToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        String newRefreshToken =
                jwtProvider.createRefreshToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                newRefreshToken,
                7,
                TimeUnit.DAYS
        );

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                user.getRole().name()
        );
    }

    public void logout(String accessToken) {

        if (!jwtProvider.validateToken(accessToken)) {

            throw new IllegalArgumentException(
                    "유효하지 않은 토큰입니다."
            );
        }

        Long userId =
                jwtProvider.getUserId(accessToken);

        redisTemplate.delete(
                "refresh:" + userId
        );

        long expiration =
                jwtProvider.getRemainingTime(accessToken);

        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    // 로그인 실패 횟수 증가
    private void increaseFailCount(
            String failKey
    ) {

        Long count =
                redisTemplate.opsForValue()
                        .increment(failKey);

        // 최초 실패 시 5분 TTL 설정
        if (count != null && count == 1) {

            redisTemplate.expire(
                    failKey,
                    5,
                    TimeUnit.MINUTES
            );
        }
    }

    public void createPasswordResetToken(
            PasswordResetRequest request
    ) {

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        request.getEmail()
                );

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now().plusMinutes(10)
                        )
                        .used(false)
                        .build();

        passwordResetTokenRepository.save(
                passwordResetToken
        );

        mailService.sendPasswordResetMail(
                user.getEmail(),
                token
        );
    }

    public void resetPassword(
            PasswordResetConfirmRequest request
    ) {

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "유효하지 않은 토큰입니다."
                                )
                        );

        // 이미 사용된 토큰
        if (token.isUsed()) {

            throw new IllegalArgumentException(
                    "이미 사용된 토큰입니다."
            );
        }

        // 만료 체크
        if (token.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "만료된 토큰입니다."
            );
        }

        User user = token.getUser();

        user.changePassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // 1회 사용 처리
        token.setUsed(true);

        passwordResetTokenRepository.save(token);
    }
}