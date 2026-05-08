package com.emr.medicare.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "verification")
public class VerificationCodeProperties {

    private List<String> doctorCodes = new ArrayList<>();
    private List<String> nurseCodes = new ArrayList<>();
}