package com.emr.medicare.auth.config;

import com.emr.medicare.auth.entity.DoctorVerificationCode;
import com.emr.medicare.auth.entity.NurseVerificationCode;
import com.emr.medicare.auth.repository.DoctorVerificationCodeRepository;
import com.emr.medicare.auth.repository.NurseVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code classpath:verification-codes.yml} 의 인증코드를 DB에 반영합니다.
 * code 가 없으면 삽입하고, 이미 있으면 JPQL UPDATE 로 {@code ownerName} 을 YAML 값으로 덮어씁니다(used 유지).
 */
@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
public class VerificationCodesYamlLoader implements ApplicationRunner {

    private static final String YAML_PATH = "classpath:verification-codes.yml";

    private final ResourceLoader resourceLoader;
    private final DoctorVerificationCodeRepository doctorVerificationCodeRepository;
    private final NurseVerificationCodeRepository nurseVerificationCodeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Resource resource = resourceLoader.getResource(YAML_PATH);
        if (!resource.exists()) {
            log.warn("인증코드 YAML 이 없습니다: {}", YAML_PATH);
            return;
        }

        Map<String, Object> root;
        try (InputStream in = resource.getInputStream()) {
            root = new Yaml().load(in);
        }
        if (root == null || root.isEmpty()) {
            log.warn("인증코드 YAML 이 비어 있습니다: {}", YAML_PATH);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> verification = (Map<String, Object>) root.get("verification");
        if (verification == null) {
            log.warn("YAML 에 'verification' 루트가 없습니다.");
            return;
        }

        String defaultOwner = stringOrEmpty(verification.get("default-owner-name"));

        Object doctors = coalesce(verification.get("doctors"), verification.get("doctor-codes"));
        Object nurses = coalesce(verification.get("nurses"), verification.get("nurse-codes"));

        int doctorInserted = 0;
        int doctorOwnerSynced = 0;
        for (CodeEntry e : parseEntries(doctors, defaultOwner)) {
            String yamlOwner = stringOrEmpty(e.ownerName());
            if (yamlOwner.isBlank()) {
                continue;
            }
            var opt = doctorVerificationCodeRepository.findByDoctorCode(e.code());
            if (opt.isEmpty()) {
                doctorVerificationCodeRepository.save(
                        DoctorVerificationCode.builder()
                                .doctorCode(e.code())
                                .ownerName(yamlOwner)
                                .used(false)
                                .build()
                );
                doctorInserted++;
            } else {
                int up = doctorVerificationCodeRepository.updateOwnerByDoctorCode(e.code(), yamlOwner);
                if (up > 0) {
                    doctorOwnerSynced++;
                } else {
                    log.warn(
                            "의사 인증코드 {} DB 행은 있으나 ownerName 동기화 UPDATE 가 0건입니다. DB의 doctor_code 값과 YAML code 가 정확히 같은지 확인하세요.",
                            e.code()
                    );
                }
            }
        }

        int nurseInserted = 0;
        int nurseOwnerSynced = 0;
        for (CodeEntry e : parseEntries(nurses, defaultOwner)) {
            String yamlOwner = stringOrEmpty(e.ownerName());
            if (yamlOwner.isBlank()) {
                continue;
            }
            var opt = nurseVerificationCodeRepository.findByNurseCode(e.code());
            if (opt.isEmpty()) {
                nurseVerificationCodeRepository.save(
                        NurseVerificationCode.builder()
                                .nurseCode(e.code())
                                .ownerName(yamlOwner)
                                .used(false)
                                .build()
                );
                nurseInserted++;
            } else {
                int up = nurseVerificationCodeRepository.updateOwnerByNurseCode(e.code(), yamlOwner);
                if (up > 0) {
                    nurseOwnerSynced++;
                } else {
                    log.warn(
                            "간호사 인증코드 {} DB 행은 있으나 ownerName 동기화 UPDATE 가 0건입니다. DB의 nurse_code 값과 YAML code 가 정확히 같은지 확인하세요.",
                            e.code()
                    );
                }
            }
        }
        log.info(
                "verification-codes.yml → DB 반영: 의사 삽입 {}·owner동기 {}, 간호사 삽입 {}·owner동기 {}",
                doctorInserted,
                doctorOwnerSynced,
                nurseInserted,
                nurseOwnerSynced
        );
    }

    private record CodeEntry(String code, String ownerName) {}

    private List<CodeEntry> parseEntries(Object node, String defaultOwner) {
        if (!(node instanceof List<?> raw)) {
            return List.of();
        }
        List<CodeEntry> out = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof String s) {
                String code = s.trim();
                if (!code.isEmpty()) {
                    out.add(new CodeEntry(code, defaultOwner.isBlank() ? "" : defaultOwner));
                }
            } else if (item instanceof Map<?, ?> m) {
                String code = stringOrEmpty(
                        coalesce(
                                m.get("code"),
                                m.get("doctorCode"),
                                m.get("doctor_code"),
                                m.get("nurseCode"),
                                m.get("nurse_code")
                        )
                );
                String owner = stringOrEmpty(coalesce(m.get("ownerName"), m.get("owner_name")));
                if (owner.isBlank()) {
                    owner = defaultOwner;
                }
                if (!code.isBlank()) {
                    out.add(new CodeEntry(code, owner));
                }
            }
        }
        return out;
    }

    private static String stringOrEmpty(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static Object coalesce(Object... xs) {
        if (xs == null) {
            return null;
        }
        for (Object x : xs) {
            if (x != null) {
                return x;
            }
        }
        return null;
    }
}
