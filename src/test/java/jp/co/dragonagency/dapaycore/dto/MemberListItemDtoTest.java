package jp.co.dragonagency.dapaycore.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MemberListItemDto} の単体テスト。
 * 単体テスト仕様書_会員一覧_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T1〜T9（■ テストケース一覧）に対応する。
 * INPUT データ:
 * C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_会員一覧_v1.00.xlsx
 */
class MemberListItemDtoTest {

    private static final LocalDateTime SUBMITTED_AT =
            LocalDateTime.of(2026, 8, 20, 10, 0);

    private static MemberListItemDto dto(
            String corporateName,
            String repLastName,
            String repFirstName,
            String applicationStatus) {
        return new MemberListItemDto(
                "MEM-0001",
                corporateName,
                repLastName,
                repFirstName,
                applicationStatus,
                SUBMITTED_AT,
                0L);
    }

    @Test
    void T1_constructor_corporateNameがnullのとき空文字になる() {
        MemberListItemDto target = dto(null, "山田", "太郎", "APPROVED");

        assertEquals("", target.getCorporateName());
    }

    @Test
    void T2_constructor_姓と名があるとき半角スペース区切りで結合する() {
        MemberListItemDto target =
                dto("株式会社テスト商事", "山田", "太郎", "APPROVED");

        assertEquals("山田 太郎", target.getRepName());
    }

    @Test
    void T3_constructor_姓のみのとき姓だけを返す() {
        MemberListItemDto target =
                dto("有限会社サンプル", "山田", null, "REVIEWING");

        assertEquals("山田", target.getRepName());
    }

    @Test
    void T4_constructor_名のみのとき名だけを返す() {
        MemberListItemDto target =
                dto("サンプル合同会社", null, "太郎", "UNREVIEWED");

        assertEquals("太郎", target.getRepName());
    }

    @Test
    void T5_constructor_姓名ともにnullのとき空文字になる() {
        MemberListItemDto target =
                dto("テスト工業株式会社", null, null, "REJECTED");

        assertEquals("", target.getRepName());
    }

    @Test
    void T6_getStatusLabel_既定のステータスを日本語ラベルに変換する() {
        assertEquals("未審査",
                dto("A", "姓", "名", "UNREVIEWED").getStatusLabel());
        assertEquals("審査中",
                dto("A", "姓", "名", "REVIEWING").getStatusLabel());
        assertEquals("承認済み",
                dto("A", "姓", "名", "APPROVED").getStatusLabel());
        assertEquals("否決",
                dto("A", "姓", "名", "REJECTED").getStatusLabel());
    }

    @Test
    void T7_getStatusLabel_未知は原文_nullは空文字を返す() {
        assertEquals("PENDING",
                dto("A", "姓", "名", "PENDING").getStatusLabel());
        assertEquals("",
                dto("A", "姓", "名", null).getStatusLabel());
    }

    @Test
    void T8_getStatusClass_ステータスに応じたバッジクラスを返す() {
        assertEquals("badge-reviewing",
                dto("A", "姓", "名", "REVIEWING").getStatusClass());
        assertEquals("badge-approved",
                dto("A", "姓", "名", "APPROVED").getStatusClass());
        assertEquals("badge-rejected",
                dto("A", "姓", "名", "REJECTED").getStatusClass());
    }

    @Test
    void T9_getStatusClass_UNREVIEWED相当はbadgeUnreviewedを返す() {
        assertEquals("badge-unreviewed",
                dto("A", "姓", "名", "UNREVIEWED").getStatusClass());
        assertEquals("badge-unreviewed",
                dto("A", "姓", "名", "PENDING").getStatusClass());
        assertEquals("badge-unreviewed",
                dto("A", "姓", "名", null).getStatusClass());
    }
}
