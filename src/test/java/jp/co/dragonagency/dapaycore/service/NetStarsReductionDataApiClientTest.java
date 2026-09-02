package jp.co.dragonagency.dapaycore.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NetStarsReductionDataApiClient} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T11〜T13（■ テストケース一覧）に対応する。
 * INPUT データ: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_還元データ取込履歴照会_v1.00.xlsx
 */
class NetStarsReductionDataApiClientTest {

    private static NetStarsReductionDataApiClient client(
            String domain, String entCode, String key, int pageSize) {
        return new NetStarsReductionDataApiClient(domain, entCode, key, pageSize);
    }

    @Test
    void T11_isConfigured_3項目すべて設定済みのときtrue() {
        assertTrue(client("https://example.com", "ENT01", "KEY01", 1000)
                .isConfigured());
    }

    @Test
    void T12_isConfigured_いずれか未設定のときfalse() {
        assertFalse(client("", "ENT01", "KEY01", 1000).isConfigured());
        assertFalse(client("https://example.com", " ", "KEY01", 1000).isConfigured());
        assertFalse(client("https://example.com", "ENT01", null, 1000).isConfigured());
    }

    @Test
    void T13_getPageSize_範囲外の値は1と1000にクランプされる() {
        assertEquals(1, client("d", "e", "k", -5).getPageSize());
        assertEquals(1, client("d", "e", "k", 0).getPageSize());
        assertEquals(1000, client("d", "e", "k", 5000).getPageSize());
        assertEquals(500, client("d", "e", "k", 500).getPageSize());
    }
}
