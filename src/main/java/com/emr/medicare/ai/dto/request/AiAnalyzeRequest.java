package com.emr.medicare.ai.dto.request;

import jakarta.validation.constraints.NotNull;

public record AiAnalyzeRequest(
        @NotNull Long userId
) {
}
