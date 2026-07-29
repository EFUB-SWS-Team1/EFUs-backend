package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.term.entity.OrganizationTerm;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationCommandService {

    private static final long INVITATION_VALID_DAYS = 7L;

    private final InvitationRepository invitationRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;


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
}