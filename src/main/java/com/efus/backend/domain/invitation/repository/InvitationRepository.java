package com.efus.backend.domain.invitation.repository;

import com.efus.backend.domain.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// TODO: term 도메인 병합 후 주석 해제
// import com.efus.backend.domain.member.entity.TermMemberRole;
// import java.util.List;

public interface InvitationRepository
        extends JpaRepository<Invitation, Long> {

    boolean existsByCode(String code);

    Optional<Invitation> findByCode(String code);

    /*
     * TODO: term 도메인 병합 후 아래 메서드 및 import 주석 해제
     *
     * 특정 기수와 역할에 해당하는 활성 초대 코드를 조회한다.
     * 초대 코드 조회 및 재발급 API에서 사용한다.
     *
     * Optional<Invitation> findByTerm_IdAndRoleAndActiveTrue(
     *         Long termId,
     *         TermMemberRole role
     * );
     */

    /*
     * TODO: term 도메인 병합 후 아래 메서드 및 import 주석 해제
     *
     * 특정 기수에 속한 활성 초대 코드를 모두 조회한다.
     * 기수 종료 시 STAFF·MEMBER 초대 코드를 비활성화하는 데 사용한다.
     *
     * List<Invitation> findAllByTerm_IdAndActiveTrue(Long termId);
     */
}