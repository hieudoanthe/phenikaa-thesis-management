package com.phenikaa.evalservice.repository;

import com.phenikaa.evalservice.entity.DefenseCommittee;
import com.phenikaa.evalservice.entity.DefenseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseCommitteeRepository extends JpaRepository<DefenseCommittee, Integer> {

    // Tìm hội đồng theo buổi bảo vệ
    List<DefenseCommittee> findByDefenseSession_SessionId(Integer sessionId);

    // Tìm hội đồng theo giảng viên
    List<DefenseCommittee> findByLecturerId(Integer lecturerId);

    // Tìm hội đồng theo buổi và vai trò
    Optional<DefenseCommittee> findByDefenseSession_SessionIdAndRole(Integer sessionId, DefenseCommittee.CommitteeRole role);
}
