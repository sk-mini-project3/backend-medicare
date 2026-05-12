package com.emr.medicare.nurse.entity;

import com.emr.medicare.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nurse_detail")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NurseDetail {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true)
    private String nurseCode;
}