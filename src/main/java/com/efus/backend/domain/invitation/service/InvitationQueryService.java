package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: organization, term, member 도메인 병합 후 주석 해제
// import com.efus.backend.domain.invitation.dto.response.InvitationCodeResponse;
// import com.efus.backend.domain.invitation.dto.response.InvitationListResponse;
// import com.efus.backend.domain.invitation.dto.response.InvitationTermResponse;
// import com.efus.backend.domain.member.entity.TermMemberRole;
// import com.efus.backend.domain.member.service.MemberQueryService;
// import com.efus.backend.domain.term.entity.OrganizationTerm;
// import com.efus.backend.domain.term.service.TermQueryService;
//
// import java.util.Comparator;
// import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationQueryService {

    private final InvitationRepository invitationRepository;

    // TODO: term, member 도메인 병합 후 주석 해제
    // private final TermQueryService termQueryService;
    // private final MemberQueryService memberQueryService;

    /*
     * TODO: organization, term, member 도메인 병합 후 주석 해제
     *
     * 현재 활성 기수의 STAFF용·MEMBER용 초대 코드를 조회한다.
     *
     * public InvitationListResponse getInvitations(Long termId) {
     *
     *     OrganizationTerm term =
     *             termQueryService.getTerm(termId);
     *
     *     memberQueryService.validateTermMember(termId);
     *     memberQueryService.validateStaff(termId);
     *     termQueryService.validateActiveTerm(termId);
     *
     *     List<InvitationCodeResponse> invitationResponses =
     *             invitationRepository
     *                     .findAllByTerm_IdAndActiveTrue(termId)
     *                     .stream()
     *                     .sorted(
     *                             Comparator.comparingInt(
     *                                     invitation ->
     *                                             getRoleOrder(
     *                                                     invitation.getRole()
     *                                             )
     *                             )
     *                     )
     *                     .map(InvitationCodeResponse::from)
     *                     .toList();
     *
     *     InvitationTermResponse termResponse =
     *             new InvitationTermResponse(
     *                     term.getId(),
     *                     term.getOrganization().getId(),
     *                     term.getOrganization().getName(),
     *                     term.getName(),
     *                     term.getStatus().name()
     *             );
     *
     *     return InvitationListResponse.of(
     *             termResponse,
     *             invitationResponses
     *     );
     * }
     */

    /*
     * TODO: member 도메인 병합 후 주석 해제
     *
     * 응답에서 STAFF 코드가 먼저,
     * MEMBER 코드가 다음에 오도록 정렬 순서를 지정한다.
     *
     * private int getRoleOrder(TermMemberRole role) {
     *     return role == TermMemberRole.STAFF ? 0 : 1;
     * }
     */
}