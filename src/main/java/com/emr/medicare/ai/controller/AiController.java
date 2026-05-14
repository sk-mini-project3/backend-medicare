package com.emr.medicare.ai.controller;

import com.emr.medicare.ai.dto.request.AiAnalyzeRequest;
import com.emr.medicare.ai.service.AiGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiGatewayService aiGatewayService;

    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts(
            @RequestParam(name = "use_rds", defaultValue = "true") boolean useRds
    ) {
        return ResponseEntity.ok(aiGatewayService.fetchAlerts(useRds));
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(
            @Valid @RequestBody AiAnalyzeRequest request,
            @RequestParam(name = "use_rds", defaultValue = "true") boolean useRds
    ) {
        return ResponseEntity.ok(aiGatewayService.analyzeUser(request.userId(), useRds));
    }
}
