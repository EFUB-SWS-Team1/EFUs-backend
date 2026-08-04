package com.efus.backend.domain.invitation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.Status;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.repository.UserRepository;
import com.efus.backend.global.security.jwt.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvitationJoinApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermMemberRepository termMemberRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private User creator;
    private User joiningUser;
    private Organization organization;
    private OrganizationTerm activeTerm;
    private TermMember creatorTermMember;
    private String accessToken;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(createUser(1001L, "creator"));
        joiningUser = userRepository.save(createUser(1002L, "joining-user"));
        organization = organizationRepository.save(
                Organization.builder()
                        .createdByUser(creator)
                        .name("EFUS")
                        .build()
        );
        activeTerm = termRepository.save(createTerm("30기", TermStatus.ACTIVE));
        creatorTermMember = termMemberRepository.save(TermMember.create(
                activeTerm,
                creator,
                TermMemberRole.STAFF,
                LocalDateTime.now().minusDays(1)
        ));
        accessToken = jwtTokenProvider.createAccessToken(joiningUser.getId());
    }

    @Test
    void memberCodeJoinsAsActiveMember() throws Exception {
        saveInvitation("EFUS-M-111111", TermMemberRole.MEMBER, true,
                LocalDateTime.now().plusDays(1), activeTerm, creatorTermMember);

        mockMvc.perform(joinRequest("EFUS-M-111111", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.termMemberId").isNumber())
                .andExpect(jsonPath("$.data.term.termId").value(activeTerm.getId()))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.joinedAt").isNotEmpty())
                .andExpect(jsonPath("$.message").value("기수 가입이 완료되었습니다."));

        TermMember savedMember = termMemberRepository
                .findByTermIdAndUserId(activeTerm.getId(), joiningUser.getId())
                .orElseThrow();
        assertThat(savedMember.getRole()).isEqualTo(TermMemberRole.MEMBER);
        assertThat(savedMember.isActive()).isTrue();
        assertThat(savedMember.getJoinedAt()).isNotNull();
    }

    @Test
    void staffCodeJoinsAsStaff() throws Exception {
        saveInvitation("EFUS-S-111111", TermMemberRole.STAFF, true,
                LocalDateTime.now().plusDays(1), activeTerm, creatorTermMember);

        mockMvc.perform(joinRequest("EFUS-S-111111", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("STAFF"));

        TermMember savedMember = termMemberRepository
                .findByTermIdAndUserId(activeTerm.getId(), joiningUser.getId())
                .orElseThrow();
        assertThat(savedMember.getRole()).isEqualTo(TermMemberRole.STAFF);
        assertThat(savedMember.isActive()).isTrue();
    }

    @Test
    void unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(joinRequest("UNKNOWN", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVITATION_NOT_FOUND"));
    }

    @Test
    void expiredCodeReturnsGone() throws Exception {
        saveInvitation("EFUS-M-EXPIRED", TermMemberRole.MEMBER, true,
                LocalDateTime.now().minusSeconds(1), activeTerm, creatorTermMember);

        mockMvc.perform(joinRequest("EFUS-M-EXPIRED", accessToken))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("INVITATION_EXPIRED"));
    }

    @Test
    void inactiveCodeReturnsGone() throws Exception {
        saveInvitation("EFUS-M-INACTIVE", TermMemberRole.MEMBER, false,
                LocalDateTime.now().plusDays(1), activeTerm, creatorTermMember);

        mockMvc.perform(joinRequest("EFUS-M-INACTIVE", accessToken))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("INVITATION_INACTIVE"));
    }

    @Test
    void closedTermReturnsTermClosed() throws Exception {
        OrganizationTerm closedTerm = termRepository.save(createTerm("29기", TermStatus.CLOSED));
        TermMember closedTermCreator = termMemberRepository.save(TermMember.create(
                closedTerm, creator, TermMemberRole.STAFF, LocalDateTime.now().minusDays(1)
        ));
        saveInvitation("EFUS-M-CLOSED", TermMemberRole.MEMBER, true,
                LocalDateTime.now().plusDays(1), closedTerm, closedTermCreator);

        mockMvc.perform(joinRequest("EFUS-M-CLOSED", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TERM_CLOSED"));
    }

    @Test
    void existingTermMemberReturnsConflict() throws Exception {
        saveInvitation("EFUS-M-DUPLICATE", TermMemberRole.MEMBER, true,
                LocalDateTime.now().plusDays(1), activeTerm, creatorTermMember);
        termMemberRepository.save(TermMember.create(
                activeTerm, joiningUser, TermMemberRole.MEMBER, LocalDateTime.now().minusHours(1)
        ));

        mockMvc.perform(joinRequest("EFUS-M-DUPLICATE", accessToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TERM_MEMBER_ALREADY_EXISTS"));
    }

    @Test
    void unauthenticatedRequestIsRejectedByExistingSecurityConfiguration() throws Exception {
        mockMvc.perform(post("/api/invitations/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"EFUS-M-111111\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void blankCodeReturnsRequiredError() throws Exception {
        mockMvc.perform(joinRequest("   ", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVITATION_CODE_REQUIRED"));
    }

    private User createUser(Long kakaoId, String name) {
        return User.builder()
                .kakaoId(kakaoId)
                .name(name)
                .status(Status.ACTIVE)
                .build();
    }

    private OrganizationTerm createTerm(String name, TermStatus status) {
        return OrganizationTerm.builder()
                .organization(organization)
                .createdByUser(creator)
                .name(name)
                .startDate(LocalDate.now().minusMonths(1))
                .termStatus(status)
                .build();
    }

    private void saveInvitation(
            String code,
            TermMemberRole role,
            boolean active,
            LocalDateTime expiresAt,
            OrganizationTerm term,
            TermMember createdBy
    ) {
        Invitation invitation = Invitation.create(term, createdBy, code, role, expiresAt);
        if (!active) {
            invitation.deactivate();
        }
        invitationRepository.save(invitation);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder joinRequest(
            String code,
            String token
    ) {
        return post("/api/invitations/join")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}");
    }
}
