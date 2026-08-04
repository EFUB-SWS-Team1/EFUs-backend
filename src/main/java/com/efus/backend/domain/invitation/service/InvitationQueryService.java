package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.dto.request.InvitationCodeRequest;
import com.efus.backend.domain.invitation.dto.response.InvitationCodeResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationListResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationTermResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationValidateResponse;
import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationQueryService {

    private final InvitationRepository invitationRepository;
    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;

    public InvitationValidateResponse validateInvitation(
            InvitationCodeRequest request
    ) {
        Invitation invitation = getValidInvitation(request);
        OrganizationTerm term = invitation.getTerm();

        return InvitationValidateResponse.from(invitation, term);
    }

    public Invitation getValidInvitation(InvitationCodeRequest request) {
        Invitation invitation = invitationRepository.findByCode(request.requireCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.isActive()) {
            throw new CustomException(ErrorCode.INVITATION_INACTIVE);
        }

        if (invitation.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVITATION_EXPIRED);
        }

        OrganizationTerm term = invitation.getTerm();
        termQueryService.validateActiveTerm(term.getId());

        return invitation;
    }

    public InvitationListResponse getInvitations(Long termId) {
        OrganizationTerm term = termQueryService.getTerm(termId);

        memberQueryService.validateStaff(termId);
        termQueryService.validateActiveTerm(termId);

        List<Invitation> invitations =
                invitationRepository.findAllByTerm_IdAndActiveTrue(termId);

        validateRequiredInvitationCodes(invitations);

        List<InvitationCodeResponse> invitationResponses = invitations.stream()
                .sorted(Comparator.comparingInt(
                        invitation -> getRoleOrder(invitation.getRole())
                ))
                .map(InvitationCodeResponse::from)
                .toList();

        InvitationTermResponse termResponse = new InvitationTermResponse(
                term.getId(),
                term.getOrganization().getId(),
                term.getOrganization().getName(),
                term.getName(),
                term.getTermStatus().name()
        );

        return InvitationListResponse.of(termResponse, invitationResponses);
    }

    private void validateRequiredInvitationCodes(List<Invitation> invitations) {
        boolean hasStaffCode = invitations.stream()
                .anyMatch(invitation -> invitation.getRole() == TermMemberRole.STAFF);
        boolean hasMemberCode = invitations.stream()
                .anyMatch(invitation -> invitation.getRole() == TermMemberRole.MEMBER);

        if (!hasStaffCode || !hasMemberCode) {
            throw new CustomException(ErrorCode.INVITATION_NOT_FOUND);
        }
    }

    private int getRoleOrder(TermMemberRole role) {
        return role == TermMemberRole.STAFF ? 0 : 1;
    }
}
