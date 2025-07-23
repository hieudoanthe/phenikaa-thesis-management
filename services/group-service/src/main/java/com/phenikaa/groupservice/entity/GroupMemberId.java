package com.phenikaa.groupservice.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GroupMemberId implements Serializable {
    private Integer groupId;
    private Integer studentId;
}