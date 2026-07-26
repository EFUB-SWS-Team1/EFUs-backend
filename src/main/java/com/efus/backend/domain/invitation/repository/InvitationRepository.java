package com.efus.backend.domain.invitation.repository;

import com.efus.backend.domain.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// TODO: term 연관관계 병합 후 주석 해제
// import com.efus.backend.domain.member.entity.TermMemberRole;
// import java.util.List;

public interface InvitationRepository
        extends JpaRepository<Invitation, Long> {

    /**
     * 동일한 초대 코드가 이미 존재하는지 확인한다.
     * 랜덤 초대 코드 생성 시 중복 검사용으로 사용한다.
     */
    boolean existsByCode(String code);

    /**
     * 초대 코드로 Invitation을 조회한다.
     *
     * 코드 검증 및 초대 코드 가입 시 사용한다.
     * 비활성화 및 만료 여부는 Service에서 구분하여 검증한다.
     */
    Optional<Invitation> findByCode(String code);

    /*
     * TODO: Invitation 엔티티의 term 연관관계 복구 후 주석 해제
     *
     * 특정 기수의 특정 역할에 해당하는 활성 초대 코드 한 건을 조회한다.
     * STAFF용 또는 MEMBER용 현재 초대 코드 조회에 사용한다.
     *
     * Optional<Invitation> findByTerm_IdAndRoleAndActiveTrue(
     *         Long termId,
     *         TermMemberRole role
     * );
     */

    /*
     * TODO: Invitation 엔티티의 term 연관관계 복구 후 주석 해제
     *
     * 특정 기수의 특정 역할에 해당하는 활성 초대 코드를 모두 조회한다.
     *
     * 재발급 전에 해당 역할의 기존 활성 코드를
     * 모두 비활성화하기 위해 사용한다.
     *
     * List<Invitation> findAllByTerm_IdAndRoleAndActiveTrue(
     *         Long termId,
     *         TermMemberRole role
     * );
     */

    /*
     * TODO: Invitation 엔티티의 term 연관관계 복구 후 주석 해제
     *
     * 특정 기수의 모든 활성 초대 코드를 조회한다.
     *
     * 초대 코드 목록 조회 및 기수 종료 시
     * 전체 초대 코드 비활성화에 사용한다.
     *
     * List<Invitation> findAllByTerm_IdAndActiveTrue(
     *         Long termId
     * );
     */
}