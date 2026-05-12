package com.emr.medicare.prescription.service;

import com.emr.medicare.common.exception.BaseException;
import com.emr.medicare.common.util.SecurityUtils;
import com.emr.medicare.prescription.dto.request.PrescriptionCreateRequest;
import com.emr.medicare.prescription.dto.response.PrescriptionResponse;
import com.emr.medicare.prescription.entity.Prescription;
import com.emr.medicare.prescription.entity.PrescriptionStatus;
import com.emr.medicare.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Transactional
    public PrescriptionResponse create(PrescriptionCreateRequest request) {
        Prescription prescription = Prescription.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .reservationId(request.getReservationId())
                .nurseId(request.getNurseId())
                .medication(request.getMedication())
                .dosage(request.getDosage())
                // 신규 처방은 항상 PENDING — APPROVED는 의사가 별도 승인 API로만 전환
                .status(PrescriptionStatus.PENDING)
                .build();

        // 1차 저장으로 prescriptionId 확보 — hash 계산에 ID 포함 필요
        Prescription saved = prescriptionRepository.save(prescription);

        // 2차: hash 세팅 후 dirty checking으로 UPDATE (추가 save() 불필요)
        saved.setHash(computeHash(saved));
        return new PrescriptionResponse(saved);
    }

    public PrescriptionResponse getById(Long prescriptionId) {
        Prescription prescription = findOrThrow(prescriptionId);

        // IDOR 방어: PATIENT는 본인 처방전만 조회 가능
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (isPatient) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (!prescription.getPatientId().equals(currentUserId)) {
                throw new BaseException(HttpStatus.FORBIDDEN, "본인의 처방전이 아닙니다.");
            }
        }

        return new PrescriptionResponse(prescription);
    }

    public List<PrescriptionResponse> getAll(PrescriptionStatus status, Long nurseId, Long doctorId) {
        return prescriptionRepository.findWithFilters(status, nurseId, doctorId).stream()
                .map(PrescriptionResponse::new)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(PrescriptionResponse::new)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getMyPrescriptions() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(PrescriptionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionResponse approve(Long prescriptionId, Long doctorId) {
        Prescription prescription = findOrThrow(prescriptionId);

        // 승인 전 위변조 검증
        if (!computeHash(prescription).equals(prescription.getHash())) {
            throw new BaseException(HttpStatus.BAD_REQUEST, "처방전 내용이 변조됐습니다.");
        }

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new BaseException(HttpStatus.BAD_REQUEST, "대기 중인 처방전만 승인할 수 있습니다.");
        }
        prescription.approve(doctorId);
        return new PrescriptionResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse reject(Long prescriptionId) {
        Prescription prescription = findOrThrow(prescriptionId);
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new BaseException(HttpStatus.BAD_REQUEST, "대기 중인 처방전만 반려할 수 있습니다.");
        }
        prescription.reject();
        return new PrescriptionResponse(prescription);
    }

    public PrescriptionResponse verify(Long prescriptionId) {
        Prescription prescription = findOrThrow(prescriptionId);
        String expectedHash = computeHash(prescription);
        if (!expectedHash.equals(prescription.getHash())) {
            // hash 불일치 → 저장 이후 DB 직접 수정 등 위변조 의심
            throw new BaseException(HttpStatus.CONFLICT, "처방전 hash 불일치 — 위변조가 의심됩니다.");
        }
        return new PrescriptionResponse(prescription);
    }

    // SHA-256(prescriptionId + patientId + medication + dosage + createdAt)
    private String computeHash(Prescription p) {
        String raw = p.getPrescriptionId()
                + String.valueOf(p.getPatientId())
                + String.valueOf(p.getMedication())
                + String.valueOf(p.getDosage())
                + p.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR, "hash 생성 실패");
        }
    }

    private Prescription findOrThrow(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "처방전을 찾을 수 없습니다."));
    }
}
