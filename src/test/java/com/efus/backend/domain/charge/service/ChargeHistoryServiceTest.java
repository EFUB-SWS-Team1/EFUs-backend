package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.request.ChargeHistoryListRequest;
import com.efus.backend.domain.charge.dto.response.ChargeHistoryListResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeHistory;
import com.efus.backend.domain.charge.repository.ChargeHistoryRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ChargeHistoryServiceTest {

    private ChargeRepository chargeRepository;
    private ChargeHistoryRepository chargeHistoryRepository;
    private MemberQueryService memberQueryService;
    private ChargeHistoryService service;

    @BeforeEach
    void setUp() {
        chargeRepository = mock(ChargeRepository.class);
        chargeHistoryRepository = mock(ChargeHistoryRepository.class);
        memberQueryService = mock(MemberQueryService.class);
        service = new ChargeHistoryService(
                chargeRepository, chargeHistoryRepository, memberQueryService);
    }

    @Test
    void returnsEmptyPageForExistingSoftDeletedCharge() {
        Long chargeId = 1L;
        Long termId = 2L;
        Charge charge = mock(Charge.class);
        OrganizationTerm term = mock(OrganizationTerm.class);
        ChargeHistoryListRequest request = new ChargeHistoryListRequest();
        request.setPage(2);
        request.setSize(3);
        PageRequest pageable = PageRequest.of(2, 3);

        when(charge.getTerm()).thenReturn(term);
        when(charge.isDeleted()).thenReturn(true);
        when(term.getId()).thenReturn(termId);
        when(chargeRepository.findById(chargeId)).thenReturn(Optional.of(charge));
        when(chargeHistoryRepository.findAllByCharge_IdOrderByChangedAtDesc(chargeId, pageable))
                .thenReturn(new PageImpl<>(List.<ChargeHistory>of(), pageable, 0));

        ChargeHistoryListResponse response = service.getChargeHistories(chargeId, request);

        assertThat(response.histories()).isEmpty();
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
        verify(memberQueryService).validateTermMember(termId);
    }

    @Test
    void rejectsUnknownCharge() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getChargeHistories(
                99L, new ChargeHistoryListRequest()))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.CHARGE_NOT_FOUND));
    }
}
