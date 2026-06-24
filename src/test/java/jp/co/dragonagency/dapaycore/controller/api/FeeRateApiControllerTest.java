package jp.co.dragonagency.dapaycore.controller.api;

import jp.co.dragonagency.dapaycore.controller.FeeRateApiController;
import jp.co.dragonagency.dapaycore.dto.FeeRateRequest;
import jp.co.dragonagency.dapaycore.dto.FeeRateResponse;
import jp.co.dragonagency.dapaycore.service.FeeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeRateApiControllerTest {

    @Mock
    private FeeRateService feeRateService;

    @InjectMocks
    private FeeRateApiController controller;

    // =========================================================
    // getMemberName（項番45〜47）
    // =========================================================

    @Test
    void T45_getMemberName_存在するmemberCodeのとき200OKとmemberNameを返す() {
        when(feeRateService.findMemberName("FE001"))
                .thenReturn("フィーテスト カブシキガイシャ");

        ResponseEntity<Map<String, Object>> result = controller.getMemberName("FE001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("フィーテスト カブシキガイシャ", result.getBody().get("memberName"));
    }

    @Test
    void T46_getMemberName_存在しないmemberCodeのとき404NotFoundを返す() {
        when(feeRateService.findMemberName("ZZZZ")).thenReturn(null);

        ResponseEntity<Map<String, Object>> result = controller.getMemberName("ZZZZ");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void T47_getMemberName_memberCodeが空白のとき404NotFoundを返す() {
        when(feeRateService.findMemberName(" ")).thenReturn(null);

        ResponseEntity<Map<String, Object>> result = controller.getMemberName(" ");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // =========================================================
    // getById（項番48〜51）
    // =========================================================

    @Test
    void T48_getById_存在するidのとき200OKと手数料レートデータを返す() {
        when(feeRateService.findById(9001L)).thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result = controller.getById(9001L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void T49_getById_レスポンスにid_memberCode_memberName_startDate_endDate_feeRateが含まれる() {
        when(feeRateService.findById(9001L)).thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result = controller.getById(9001L);

        Map<String, Object> body = result.getBody();
        assertTrue(body.containsKey("id"));
        assertTrue(body.containsKey("memberCode"));
        assertTrue(body.containsKey("memberName"));
        assertTrue(body.containsKey("startDate"));
        assertTrue(body.containsKey("endDate"));
        assertTrue(body.containsKey("feeRate"));
    }

    @Test
    void T50_getById_存在しないidのとき404NotFoundを返す() {
        when(feeRateService.findById(99999L))
                .thenThrow(new IllegalArgumentException("手数料レートが見つかりません: 99999"));

        ResponseEntity<Map<String, Object>> result = controller.getById(99999L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void T51_getById_サービス層で予期しない例外が発生したとき500を返す() {
        when(feeRateService.findById(anyLong()))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result = controller.getById(9001L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // create（項番52〜54）
    // =========================================================

    @Test
    void T52_create_有効なリクエストのとき200OKと登録されたレートデータを返す() {
        when(feeRateService.create(any(FeeRateRequest.class))).thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("FE001", result.getBody().get("memberCode"));
    }

    @Test
    void T53_create_バリデーションエラーのとき400BadRequestとmessageを返す() {
        when(feeRateService.create(any(FeeRateRequest.class)))
                .thenThrow(new IllegalArgumentException("会員コードは必須です"));

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest());

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("会員コードは必須です"));
    }

    @Test
    void T54_create_サービス層で予期しない例外が発生したとき500を返す() {
        when(feeRateService.create(any(FeeRateRequest.class)))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // update（項番55〜57）
    // =========================================================

    @Test
    void T55_update_有効なリクエストのとき200OKと更新されたレートデータを返す() {
        when(feeRateService.update(anyLong(), any(FeeRateRequest.class)))
                .thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result =
                controller.update(9001L, buildRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void T56_update_バリデーションエラーまたは存在しないidのとき400BadRequestを返す() {
        when(feeRateService.update(anyLong(), any(FeeRateRequest.class)))
                .thenThrow(new IllegalArgumentException("手数料レートが見つかりません"));

        ResponseEntity<Map<String, Object>> result =
                controller.update(99999L, buildRequest());

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("手数料レートが見つかりません"));
    }

    @Test
    void T57_update_サービス層で予期しない例外が発生したとき500を返す() {
        when(feeRateService.update(anyLong(), any(FeeRateRequest.class)))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result =
                controller.update(9001L, buildRequest());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // delete（項番58〜60）
    // =========================================================

    @Test
    void T58_delete_存在するidのとき200OKとsuccessTrueを返す() {
        ResponseEntity<Map<String, Object>> result = controller.delete(9001L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
    }

    @Test
    void T59_delete_存在しないidのとき404NotFoundとmessageを返す() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("手数料レートが見つかりません"))
                .when(feeRateService).delete(99999L);

        ResponseEntity<Map<String, Object>> result = controller.delete(99999L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("手数料レートが見つかりません"));
    }

    @Test
    void T60_delete_サービス層で予期しない例外が発生したとき500を返す() {
        org.mockito.Mockito.doThrow(new RuntimeException("予期しないエラー"))
                .when(feeRateService).delete(anyLong());

        ResponseEntity<Map<String, Object>> result = controller.delete(9001L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private static FeeRateResponse buildResponse() {
        FeeRateResponse r = new FeeRateResponse();
        r.setId(9001L);
        r.setMemberCode("FE001");
        r.setMemberName("フィーテスト カブシキガイシャ");
        r.setStartDate("2026-01-01");
        r.setEndDate("2026-12-31");
        r.setFeeRate(new BigDecimal("3.24"));
        return r;
    }

    private static FeeRateRequest buildRequest() {
        FeeRateRequest req = new FeeRateRequest();
        req.setMemberCode("FE001");
        req.setStartDate("2026-01-01");
        req.setEndDate("2026-12-31");
        req.setFeeRate(new BigDecimal("3.24"));
        return req;
    }
}
