package com.phenikaa.groupservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@IdClass(GroupMemberId.class)
public class GroupMember {
    @Id
    @Column(name = "group_id")
    private Integer groupId;

    @Id
    @Column(name = "student_id")
    private Integer studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private StudentGroup group;

    @Column(name = "role")
    private Integer role;

    @Column(name = "is_leader")
    private Boolean isLeader;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "membership_status")
    private Integer membershipStatus;
}
