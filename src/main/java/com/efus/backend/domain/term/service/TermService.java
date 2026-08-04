package com.efus.backend.domain.term.service;

import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.dto.request.TermCloseRequest;
import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.request.TermUpdateRequest;
import com.efus.backend.domain.term.dto.response.TermCloseResponse;
import com.efus.backend.domain.term.dto.response.TermCreateResponse;
import com.efus.backend.domain.term.dto.response.TermListResponse;
import com.efus.backend.domain.term.dto.response.TermUpdateResponse;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
// import com.efus.backend.domain.organization.service.OrganizationQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TermService {
    private final CurrentUserService currentUserService;
    private final TermRepository termRepository;
    private final TermMemberRepository termMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final InvitationCommandService invitationCommandService;

    // [다음 기수 생성]
    @Transactional
    public TermCreateResponse createTerm(Long organizationId, TermCreateRequest request) {
        User user = currentUserService.getCurrentUser();

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        validateStaffInRecentTerm(organizationId, user.getId());

        boolean hasActiveTerm = termRepository.existsByOrganizationIdAndTermStatus(organizationId, TermStatus.ACTIVE);
        if (hasActiveTerm) {
            throw new CustomException(ErrorCode.ACTIVE_TERM_ALREADY_EXISTS);
        }

        // 위 검증들을 다 통과하면 새로운 기수 생성
        OrganizationTerm term = request.toEntity(organization, user);
        termRepository.save(term);
        TermMember staffMember = TermMember.create(
                term,
                user,
                TermMemberRole.STAFF,
                LocalDateTime.now()
        );
        termMemberRepository.save(staffMember);
        invitationCommandService.createDefaultInvitations(term, staffMember);

         return TermCreateResponse.of(term, organization, staffMember);
    }

    @Transactional
    public TermUpdateResponse updateTerm(Long termId, TermUpdateRequest request) {
        // 1. 요청 값 검증: 수정할 필드가 하나도 없는 경우 처리 (400 EMPTY_UPDATE_REQUEST)
        if (request.isEmpty()) {
            // throw new CustomException(ErrorCode.EMPTY_UPDATE_REQUEST);
        }

        User user = currentUserService.getCurrentUser();

        // 2. 기수 조회 (없으면 404 TERM_NOT_FOUND)
        // OrganizationTerm term = termQueryService.getTerm(termId);
        OrganizationTerm term = null; // 임시

        // TODO 3. 상태 검증: 이미 종료된 기수인지 확인 (409 TERM_ALREADY_CLOSED)
        if (term.getTermStatus() == TermStatus.CLOSED) {
            // throw new CustomException(ErrorCode.TERM_ALREADY_CLOSED);
        }

        // TODO 4. 권한 검증: 해당 기수의 STAFF인지 확인 (403 STAFF_PERMISSION_REQUIRED)

        // TODO 5. 정보 수정 (Dirty Checking)
        // 값이 들어온 필드만(null이 아닌 경우만) 기존 엔티티의 값을 변경합니다.
        // term.update(request.name(), request.startDate()); // OrganizationTerm 엔티티 내부에 update 메서드 생성 필요

        // 6. 뼈대 반환
        return TermUpdateResponse.from(term);
    }

    @Transactional
    public TermCloseResponse closeTerm(Long termId, TermCloseRequest request) {
        User user = currentUserService.getCurrentUser();

        // 1. 기수 조회 (없으면 404 TERM_NOT_FOUND)
        OrganizationTerm term = null; // 임시: termQueryService.getTerm(termId);

        // TODO 2. 상태 검증: 이미 종료된 기수인지 확인 (409 TERM_ALREADY_CLOSED)
        if (term.getTermStatus() == TermStatus.CLOSED) {
            // throw new CustomException(ErrorCode.TERM_ALREADY_CLOSED);
        }

        // TODO 3. 권한 검증: 요청자가 해당 기수의 STAFF인지 확인 (403 STAFF_PERMISSION_REQUIRED)
        // 권한 확인 후 해당 요청자의 TermMember 객체(closer)를 가져와야 합니다.

        // TODO 4. 날짜 검증: 기수 종료일이 기수 시작일보다 빠른지 확인 (400 END_DATE_BEFORE_START_DATE)
        if (request.endDate().isBefore(term.getStartDate())) {
            // throw new CustomException(ErrorCode.END_DATE_BEFORE_START_DATE);
        }

        // 5. 서버 처리 내용 1, 2: 기수 상태를 CLOSED로 변경 및 종료일 저장
        // term.close(request.endDate()); // 엔티티 내부에 상태 변경 메서드 구현 필요

        // TODO 6. 서버 처리 내용 3: 해당 기수의 STAFF·MEMBER 초대 코드 비활성화
        // invitationService.deactivateCodesByTerm(term);

        // TODO 7. 서버 처리 내용 4: 기수 종료 이력 저장
        // termHistoryRepository.save(...);

        // (서버 처리 내용 5: 쓰기 작업 차단은 이후 다른 API들의 인터셉터나 서비스 검증 로직에서 활성 기수인지 체크하는 방식으로 적용됩니다)

        // 8. 뼈대 반환
        // return TermCloseResponse.of(term, closer);
        return null;
    }

    private void validateStaffInRecentTerm(Long organizationId, Long userId) {
        // 단체의 가장 최근 종료된 기수 조회
        OrganizationTerm recentTerm = termRepository.findTopByOrganizationIdAndTermStatusOrderByEndDateDesc(organizationId, TermStatus.CLOSED)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVE_TERM_ALREADY_EXISTS));

        // 가장 최근 기수에 해당 유저가 소속되어 있었는지 확인
        TermMember recentTermMember = termMemberRepository.findByTermIdAndUserId(recentTerm.getId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_CREATION_FORBIDDEN));

        // 소속되어 있었다면, 그 역할이 STAFF인지 확인
        if (recentTermMember.getRole() != TermMemberRole.STAFF) {
            throw new CustomException(ErrorCode.TERM_CREATION_FORBIDDEN);
        }
    }
}