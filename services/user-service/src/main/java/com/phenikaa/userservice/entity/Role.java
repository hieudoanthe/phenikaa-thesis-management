package com.phenikaa.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "[roles]", schema = "HieuDT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private RoleName roleName;

    public enum RoleName {
        STUDENT, ADMIN, TEACHER
    }

    @Column(name = "is_active")
    private Boolean isActive;
}
