package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.ContactInquiryListItemDto;
import jp.co.dragonagency.dapaycore.model.ContactInquiry;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.ContactInquiryRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ContactInquiryService} の単体テスト。
 * 単体テスト仕様書_お問い合わせ履歴_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T1〜T10（■ テストケース一覧）に対応する。
 * INPUT データ: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_お問い合わせ履歴_v1.00.xlsx
 */
@ExtendWith(MockitoExtension.class)
class ContactInquiryServiceTest {

    @Mock
    private ContactInquiryRepository contactInquiryRepository;

    @Mock
    private MerchantApplicationRepository merchantApplicationRepository;

    @InjectMocks
    private ContactInquiryService service;

    @Test
    void T1_findAllInquiriesForOperation_データが0件のとき空リストを返す() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        List<ContactInquiryListItemDto> result =
                service.findAllInquiriesForOperation();

        assertTrue(result.isEmpty());
        verify(merchantApplicationRepository, never()).findAllById(any());
    }

    @Test
    void T2_findAllInquiriesForOperation_受付日時の降順を維持して返す() {
        ContactInquiry newest = inquiry("INQ-0024", null,
                LocalDateTime.of(2026, 8, 25, 10, 0));
        ContactInquiry middle = inquiry("INQ-0012", null,
                LocalDateTime.of(2026, 8, 13, 10, 0));
        ContactInquiry oldest = inquiry("INQ-0001", null,
                LocalDateTime.of(2026, 8, 2, 10, 0));
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(newest, middle, oldest));

        List<ContactInquiryListItemDto> result =
                service.findAllInquiriesForOperation();

        assertEquals(List.of("INQ-0024", "INQ-0012", "INQ-0001"),
                result.stream().map(ContactInquiryListItemDto::getInquiryNumber)
                        .toList());
    }

    @Test
    void T3_findAllInquiriesForOperation_各項目がDTOへ写される() {
        ContactInquiry inq = new ContactInquiry();
        inq.setInquiryNumber("INQ-0001");
        inq.setMemberCode(null);
        inq.setCategory("請求・精算について");
        inq.setSubject("決済手数料の内訳について");
        inq.setBody("今月の請求に記載された決済手数料の計算根拠を教えてください。");
        inq.setStatus(ContactInquiry.STATUS_RECEIVED);
        inq.setCreatedAt(LocalDateTime.of(2026, 8, 2, 9, 7));
        inq.setUpdatedAt(LocalDateTime.of(2026, 8, 2, 13, 11));
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inq));

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("INQ-0001", dto.getInquiryNumber());
        assertEquals("請求・精算について", dto.getCategory());
        assertEquals("決済手数料の内訳について", dto.getSubject());
        assertEquals("今月の請求に記載された決済手数料の計算根拠を教えてください。",
                dto.getBody());
        assertEquals(ContactInquiry.STATUS_RECEIVED, dto.getStatus());
        assertEquals(LocalDateTime.of(2026, 8, 2, 9, 7), dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 8, 2, 13, 11), dto.getUpdatedAt());
    }

    @Test
    void T4_findAllInquiriesForOperation_memberCodeがある行は法人名カナを付加する() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inquiry("INQ-0001", "MEM-0001",
                        LocalDateTime.of(2026, 8, 2, 10, 0))));
        when(merchantApplicationRepository.findAllById(any()))
                .thenReturn(List.of(
                        application("MEM-0001", "カブシキガイシャ　テストショウジ")));

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("MEM-0001", dto.getMemberCode());
        assertEquals("カブシキガイシャ　テストショウジ", dto.getCorporateNameKana());
    }

    @Test
    void T5_findAllInquiriesForOperation_memberCodeがnullの行は会員コードと法人名カナが空文字() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inquiry("INQ-0004", null,
                        LocalDateTime.of(2026, 8, 5, 10, 0))));

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("", dto.getMemberCode());
        assertEquals("", dto.getCorporateNameKana());
        verify(merchantApplicationRepository, never()).findAllById(any());
    }

    @Test
    void T6_findAllInquiriesForOperation_memberCodeが空文字の行は会員コードと法人名カナが空文字() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inquiry("INQ-0005", "",
                        LocalDateTime.of(2026, 8, 6, 10, 0))));

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("", dto.getMemberCode());
        assertEquals("", dto.getCorporateNameKana());
        verify(merchantApplicationRepository, never()).findAllById(any());
    }

    @Test
    void T7_findAllInquiriesForOperation_対応する申込が無い行は法人名カナが空文字() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inquiry("INQ-0003", "MEM-9999",
                        LocalDateTime.of(2026, 8, 4, 10, 0))));
        when(merchantApplicationRepository.findAllById(any()))
                .thenReturn(Collections.emptyList());

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("MEM-9999", dto.getMemberCode());
        assertEquals("", dto.getCorporateNameKana());
    }

    @Test
    void T8_findAllInquiriesForOperation_申込のcorporateNameKanaがnullの行は法人名カナが空文字() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(inquiry("INQ-0002", "MEM-0002",
                        LocalDateTime.of(2026, 8, 3, 10, 0))));
        when(merchantApplicationRepository.findAllById(any()))
                .thenReturn(List.of(application("MEM-0002", null)));

        ContactInquiryListItemDto dto =
                service.findAllInquiriesForOperation().get(0);

        assertEquals("", dto.getCorporateNameKana());
    }

    @Test
    void T9_findAllInquiriesForOperation_同一memberCodeは重複排除してfindAllByIdへ渡す() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(
                        inquiry("INQ-0001", "MEM-0001",
                                LocalDateTime.of(2026, 8, 2, 10, 0)),
                        inquiry("INQ-0006", "MEM-0001",
                                LocalDateTime.of(2026, 8, 7, 10, 0))));
        when(merchantApplicationRepository.findAllById(any()))
                .thenReturn(Collections.emptyList());

        service.findAllInquiriesForOperation();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<String>> captor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(merchantApplicationRepository).findAllById(captor.capture());
        List<String> codes = new ArrayList<>();
        captor.getValue().forEach(codes::add);
        assertEquals(List.of("MEM-0001"), codes);
    }

    @Test
    void T10_findAllInquiriesForOperation_memberCodeが全て無いときfindAllByIdを呼ばない() {
        when(contactInquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(
                        inquiry("INQ-0004", null,
                                LocalDateTime.of(2026, 8, 5, 10, 0)),
                        inquiry("INQ-0005", "",
                                LocalDateTime.of(2026, 8, 6, 10, 0))));

        service.findAllInquiriesForOperation();

        verify(merchantApplicationRepository, never()).findAllById(any());
    }

    private static ContactInquiry inquiry(
            String number, String memberCode, LocalDateTime createdAt) {
        ContactInquiry inq = new ContactInquiry();
        inq.setInquiryNumber(number);
        inq.setMemberCode(memberCode);
        inq.setCategory("その他");
        inq.setSubject("件名 " + number);
        inq.setBody("本文 " + number);
        inq.setStatus(ContactInquiry.STATUS_RECEIVED);
        inq.setCreatedAt(createdAt);
        inq.setUpdatedAt(createdAt);
        return inq;
    }

    private static MerchantApplication application(
            String memberCode, String corporateNameKana) {
        MerchantApplication app = new MerchantApplication();
        app.setMemberCode(memberCode);
        app.setCorporateNameKana(corporateNameKana);
        return app;
    }
}
