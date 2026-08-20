package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;

/**
 * MerchantApplicationInquiryController の単体テスト。
 * 単体テスト仕様書_申込内容照会画面_v1.00.xlsx の項番76〜81に対応する。
 */
@ExtendWith(MockitoExtension.class)
class MerchantApplicationInquiryControllerTest {

    @Mock
    private MerchantApplicationInquiryService inquiryService;

    @InjectMocks
    private MerchantApplicationInquiryController controller;

    // =========================================================
    // update（項番76〜78）
    // =========================================================

    @Test
    void T76_update_正常な更新リクエストのとき200OKとsuccessTrueが返る() {
        Map<String, String> data = Map.of("memberCode", "TEST0001");

        ResponseEntity<Map<String, Object>> result = controller.update(data);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
        verify(inquiryService).updateApplication(data);
    }

    @Test
    void T77_update_バリデーションエラーのとき400BadRequestとsuccessFalseが返る() {
        Map<String, String> data = Map.of("memberCode", "TEST0001");
        Mockito.doThrow(new IllegalArgumentException("会員コードが指定されていません"))
                .when(inquiryService).updateApplication(data);

        ResponseEntity<Map<String, Object>> result = controller.update(data);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(false, result.getBody().get("success"));
        assertEquals("会員コードが指定されていません", result.getBody().get("message"));
    }

    @Test
    void T78_update_サービス層で予期しない例外が発生したとき500InternalServerErrorとsuccessFalseが返る() {
        Map<String, String> data = Map.of("memberCode", "TEST0001");
        Mockito.doThrow(new RuntimeException("予期しないエラー"))
                .when(inquiryService).updateApplication(anyMap());

        ResponseEntity<Map<String, Object>> result = controller.update(data);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(false, result.getBody().get("success"));
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // delete（項番79〜81）
    // =========================================================

    @Test
    void T79_delete_正常な削除リクエストのとき200OKとsuccessTrueが返る() {
        Map<String, String> data = Map.of("memberCode", "TEST0001");

        ResponseEntity<Map<String, Object>> result = controller.delete(data);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
        verify(inquiryService).deleteApplication("TEST0001");
    }

    @Test
    void T80_delete_memberCode不正のとき400BadRequestとsuccessFalseが返る() {
        Map<String, String> data = Map.of("memberCode", "NOTFOUND");
        Mockito.doThrow(new IllegalArgumentException("申込情報が見つかりません: NOTFOUND"))
                .when(inquiryService).deleteApplication("NOTFOUND");

        ResponseEntity<Map<String, Object>> result = controller.delete(data);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(false, result.getBody().get("success"));
    }

    @Test
    void T81_delete_サービス層で予期しない例外が発生したとき500InternalServerErrorとsuccessFalseが返る() {
        Map<String, String> data = Map.of("memberCode", "TEST0001");
        Mockito.doThrow(new RuntimeException("予期しないエラー"))
                .when(inquiryService).deleteApplication("TEST0001");

        ResponseEntity<Map<String, Object>> result = controller.delete(data);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(false, result.getBody().get("success"));
        assertNotNull(result.getBody().get("message"));
    }
}
