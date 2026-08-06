package com.efus.backend.domain.charge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.user.entity.Status;
import com.efus.backend.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class ChargeRepositoryTest {

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private ChargeMemberRepository chargeMemberRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private OrganizationTerm term;
    private OrganizationTerm otherTerm;
    private TermMember creator;
    private Funding funding;

    @BeforeEach
    void setUp() {
        User owner = saveUser(1L, "owner");
        Organization organization = entityManager.persistAndFlush(
                Organization.builder().createdByUser(owner).name("organization").build());
        term = entityManager.persistAndFlush(OrganizationTerm.builder()
                .organization(organization)
                .createdByUser(owner)
                .name("term")
                .startDate(LocalDate.of(2026, 1, 1))
                .termStatus(TermStatus.ACTIVE)
                .build());
        otherTerm = entityManager.persistAndFlush(OrganizationTerm.builder()
                .organization(organization)
                .createdByUser(owner)
                .name("other term")
                .startDate(LocalDate.of(2026, 1, 1))
                .termStatus(TermStatus.ACTIVE)
                .build());
        creator = entityManager.persistAndFlush(TermMember.create(
                term, owner, TermMemberRole.STAFF, LocalDateTime.of(2026, 1, 1, 0, 0)));
        funding = entityManager.persistAndFlush(Funding.builder()
                .organizationTerm(term)
                .createdByTermMember(creator)
                .name("funding")
                .budgetAmount(100_000L)
                .participantCount(2)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build());
    }

    @Test
    void appliesInclusiveDateRangeAndTermFilter() {
        Charge fromBoundary = saveCharge(term, null, "from", LocalDate.of(2026, 8, 1), 10_000L);
        Charge toBoundary = saveCharge(term, null, "to", LocalDate.of(2026, 8, 31), 10_000L);
        saveCharge(term, null, "before", LocalDate.of(2026, 7, 31), 10_000L);
        saveCharge(term, null, "after", LocalDate.of(2026, 9, 1), 10_000L);
        TermMember otherCreator = entityManager.persistAndFlush(TermMember.create(
                otherTerm, saveUser(2L, "other"), TermMemberRole.STAFF,
                LocalDateTime.of(2026, 1, 1, 0, 0)));
        saveCharge(otherTerm, null, otherCreator, "other term", LocalDate.of(2026, 8, 15), 10_000L);

        List<LedgerChargeDto> result = chargeRepository.findLedgerCharges(
                term.getId(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), false, null);

        assertThat(result).extracting(LedgerChargeDto::chargeId)
                .containsExactly(toBoundary.getId(), fromBoundary.getId());
    }

    @Test
    void includesDeletedOnlyWhenRequestedAndFiltersOptionalFunding() {
        Charge funded = saveCharge(term, funding, "funded", LocalDate.of(2026, 8, 20), 10_000L);
        Charge withoutFunding = saveCharge(term, null, "without funding", LocalDate.of(2026, 8, 19), 10_000L);
        Charge deleted = saveCharge(term, funding, "deleted", LocalDate.of(2026, 8, 18), 10_000L);
        deleted.softDelete(creator, LocalDateTime.of(2026, 8, 6, 12, 0));
        entityManager.flush();

        List<LedgerChargeDto> active = chargeRepository.findLedgerCharges(
                term.getId(), null, null, false, null);
        List<LedgerChargeDto> includingDeleted = chargeRepository.findLedgerCharges(
                term.getId(), null, null, true, null);
        List<LedgerChargeDto> fundedOnly = chargeRepository.findLedgerCharges(
                term.getId(), null, null, true, funding.getId());

        assertThat(active).extracting(LedgerChargeDto::chargeId)
                .containsExactly(funded.getId(), withoutFunding.getId());
        assertThat(active).filteredOn(dto -> dto.chargeId().equals(withoutFunding.getId()))
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.fundingId()).isNull();
                    assertThat(dto.fundingName()).isNull();
                });
        assertThat(includingDeleted).extracting(LedgerChargeDto::chargeId)
                .containsExactly(funded.getId(), withoutFunding.getId(), deleted.getId());
        assertThat(fundedOnly).extracting(LedgerChargeDto::chargeId)
                .containsExactly(funded.getId(), deleted.getId());
    }

    @Test
    void groupsPaidAmountsPerChargeAndReturnsZeroWithoutMembers() {
        Charge partiallyPaid = saveCharge(term, null, "partial", LocalDate.of(2026, 8, 20), 30_000L);
        Charge paid = saveCharge(term, null, "paid", LocalDate.of(2026, 8, 19), 20_000L);
        Charge noMembers = saveCharge(term, null, "empty", LocalDate.of(2026, 8, 18), 15_000L);
        TermMember member1 = saveMember(3L, "member1");
        TermMember member2 = saveMember(4L, "member2");
        TermMember member3 = saveMember(5L, "member3");
        saveChargeMember(partiallyPaid, member1, 10_000L, true);
        saveChargeMember(partiallyPaid, member2, 10_000L, true);
        saveChargeMember(partiallyPaid, member3, 10_000L, false);
        saveChargeMember(paid, member1, 10_000L, true);
        saveChargeMember(paid, member2, 10_000L, true);

        List<LedgerChargeDto> result = chargeRepository.findLedgerCharges(
                term.getId(), null, null, false, null);

        assertThat(result).filteredOn(dto -> dto.chargeId().equals(partiallyPaid.getId()))
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.paidAmount()).isEqualTo(20_000L);
                    assertThat(dto.unpaidAmount()).isEqualTo(10_000L);
                    assertThat(dto.paymentStatus()).isEqualTo(ChargePaymentStatus.PARTIALLY_PAID);
                });
        assertThat(result).filteredOn(dto -> dto.chargeId().equals(paid.getId()))
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.paidAmount()).isEqualTo(20_000L);
                    assertThat(dto.unpaidAmount()).isZero();
                    assertThat(dto.paymentStatus()).isEqualTo(ChargePaymentStatus.PAID);
                });
        assertThat(result).filteredOn(dto -> dto.chargeId().equals(noMembers.getId()))
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.paidAmount()).isZero();
                    assertThat(dto.unpaidAmount()).isEqualTo(15_000L);
                    assertThat(dto.paymentStatus()).isEqualTo(ChargePaymentStatus.UNPAID);
                });
    }

    @Test
    void sortsByDueDateThenCreatedAtDescending() {
        Charge earlierDueDate = saveCharge(
                term, null, creator, "earlier due", LocalDate.of(2026, 8, 19), 10_000L,
                LocalDateTime.of(2026, 8, 3, 0, 0));
        Charge older = saveCharge(
                term, null, creator, "older", LocalDate.of(2026, 8, 20), 10_000L,
                LocalDateTime.of(2026, 8, 1, 0, 0));
        Charge newer = saveCharge(
                term, null, creator, "newer", LocalDate.of(2026, 8, 20), 10_000L,
                LocalDateTime.of(2026, 8, 2, 0, 0));

        List<LedgerChargeDto> result = chargeRepository.findLedgerCharges(
                term.getId(), null, null, false, null);

        assertThat(result).extracting(LedgerChargeDto::chargeId)
                .containsExactly(newer.getId(), older.getId(), earlierDueDate.getId());
    }

    private User saveUser(Long kakaoId, String name) {
        return entityManager.persistAndFlush(User.builder()
                .kakaoId(kakaoId)
                .email(name + "@example.com")
                .name(name)
                .status(Status.ACTIVE)
                .build());
    }

    private TermMember saveMember(Long kakaoId, String name) {
        return entityManager.persistAndFlush(TermMember.create(
                term, saveUser(kakaoId, name), TermMemberRole.MEMBER,
                LocalDateTime.of(2026, 1, 1, 0, 0)));
    }

    private Charge saveCharge(
            OrganizationTerm chargeTerm, Funding chargeFunding, String title,
            LocalDate dueDate, Long requestedAmount
    ) {
        return saveCharge(chargeTerm, chargeFunding, creator, title, dueDate, requestedAmount);
    }

    private Charge saveCharge(
            OrganizationTerm chargeTerm, Funding chargeFunding, TermMember chargeCreator,
            String title, LocalDate dueDate, Long requestedAmount
    ) {
        Charge charge = Charge.create(
                chargeTerm, chargeFunding, chargeCreator, title,
                ChargeMethod.PER_PERSON, dueDate, null, requestedAmount);
        return entityManager.persistAndFlush(charge);
    }

    private Charge saveCharge(
            OrganizationTerm chargeTerm, Funding chargeFunding, TermMember chargeCreator,
            String title, LocalDate dueDate, Long requestedAmount, LocalDateTime createdAt
    ) {
        Charge charge = Charge.create(
                chargeTerm, chargeFunding, chargeCreator, title,
                ChargeMethod.PER_PERSON, dueDate, null, requestedAmount);
        ReflectionTestUtils.setField(charge, "createdAt", createdAt);
        return entityManager.persistAndFlush(charge);
    }

    private void saveChargeMember(
            Charge charge, TermMember member, Long assignedAmount, boolean paid
    ) {
        ChargeMember chargeMember = ChargeMember.create(charge, member, assignedAmount);
        if (paid) {
            chargeMember.markAsPaid(LocalDateTime.of(2026, 8, 1, 0, 0));
        }
        chargeMemberRepository.saveAndFlush(chargeMember);
    }
}
