package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.dto.request.InvitationReissueRequest;
import com.efus.backend.domain.invitation.dto.response.InvitationReissueResponse;
import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.term.entity.OrganizationTerm;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationCommandService {

    private static final long INVITATION_VALID_DAYS = 7L;

    private final InvitationRepository invitationRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;
    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;


//  단체의 첫 기수 또는 다음 기수를 생성할 때
//  STAFF용·MEMBER용 초대 코드를 자동으로 생성한다.

    public void createDefaultInvitations(
            OrganizationTerm term,
            TermMember creatorTermMember
    ) {
        createInvitation(
                term,
                creatorTermMember,
                TermMemberRole.STAFF
        );

        createInvitation(
                term,
                creatorTermMember,
                TermMemberRole.MEMBER
        );
    }

    public InvitationReissueResponse reissueInvitation(
            Long termId,
            InvitationReissueRequest request
    ) {
        OrganizationTerm term = termQueryService.getTerm(termId);
        TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);

        if (!currentTermMember.isStaff()) {
            throw new CustomException(ErrorCode.STAFF_REQUIRED);
        }

        termQueryService.validateActiveTerm(termId);
        TermMemberRole role = request.toRole();

        invitationRepository.findAllByTerm_IdAndRoleAndActiveTrue(termId, role)
                .forEach(Invitation::deactivate);

        Invitation invitation = createInvitation(term, currentTermMember, role);
        return InvitationReissueResponse.from(invitation);
    }


    private Invitation createInvitation(
            OrganizationTerm term,
            TermMember creatorTermMember,
            TermMemberRole role
    ) {
        String code = invitationCodeGenerator.generate(role);

        Invitation invitation = Invitation.create(
                term,
                creatorTermMember,
                code,
                role,
                LocalDateTime.now().plusDays(INVITATION_VALID_DAYS)
        );

        return invitationRepository.save(invitation);
    }

    public void deactivateCodesByTermId(Long termId) {
        List<Invitation> activeInvitations = invitationRepository.findAllByTerm_IdAndActiveTrue(termId);

        for (Invitation invitation : activeInvitations) {
            invitation.deactivate();
        }
    }
}
