package jp.co.dragonagency.dapaycore.controller.api;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.controller.TransferFeeApiController;
import jp.co.dragonagency.dapaycore.dto.TransferFeeRequest;
import jp.co.dragonagency.dapaycore.dto.TransferFeeResponse;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferFeeApiControllerTest {

    @Mock
    private TransferFeeService transferFeeService;

    @InjectMocks
    private TransferFeeApiController controller;

    // =========================================================
    // getByBankCode（項番29〜31）
    // =========================================================

    @Test
    void T29_getByBankCode_存在するbankCodeのとき200OKとデータを返す() {
        when(transferFeeService.findByBankCode("0310")).thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result = controller.getByBankCode("0310");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("0310", result.getBody().get("bankCode"));
    }

    @Test
    void T30_getByBankCode_存在しないbankCodeのとき404NotFoundを返す() {
        when(transferFeeService.findByBankCode("9999"))
                .thenThrow(new IllegalArgumentException("振込手数料が見つかりません: 9999"));

        ResponseEntity<Map<String, Object>> result = controller.getByBankCode("9999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void T31_getByBankCode_サービス層で予期しない例外が発生したとき500を返す() {
        when(transferFeeService.findByBankCode(anyString()))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result = controller.getByBankCode("0310");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // create（項番32〜34）
    // =========================================================

    @Test
    void T32_create_有効なリクエストのとき200OKと登録されたデータを返す() {
        when(transferFeeService.create(any(TransferFeeRequest.class), anyString()))
                .thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("0310", result.getBody().get("bankCode"));
    }

    @Test
    void T33_create_バリデーションエラーのとき400BadRequestとmessageを返す() {
        when(transferFeeService.create(any(TransferFeeRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("銀行コードは必須です"));

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("銀行コードは必須です"));
    }

    @Test
    void T34_create_サービス層で予期しない例外が発生したとき500を返す() {
        when(transferFeeService.create(any(TransferFeeRequest.class), anyString()))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result =
                controller.create(buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // update（項番35〜37）
    // =========================================================

    @Test
    void T35_update_有効なリクエストのとき200OKと更新されたデータを返す() {
        when(transferFeeService.update(eq("0310"), any(TransferFeeRequest.class), anyString()))
                .thenReturn(buildResponse());

        ResponseEntity<Map<String, Object>> result =
                controller.update("0310", buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void T36_update_バリデーションエラーまたは存在しないbankCodeのとき400BadRequestを返す() {
        when(transferFeeService.update(eq("9999"), any(TransferFeeRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("振込手数料が見つかりません"));

        ResponseEntity<Map<String, Object>> result =
                controller.update("9999", buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("振込手数料が見つかりません"));
    }

    @Test
    void T37_update_サービス層で予期しない例外が発生したとき500を返す() {
        when(transferFeeService.update(eq("0310"), any(TransferFeeRequest.class), anyString()))
                .thenThrow(new RuntimeException("予期しないエラー"));

        ResponseEntity<Map<String, Object>> result =
                controller.update("0310", buildRequest(), buildSession("user001"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // delete（項番38〜40）
    // =========================================================

    @Test
    void T38_delete_存在するbankCodeのとき200OKとsuccessTrueを返す() {
        ResponseEntity<Map<String, Object>> result = controller.delete("0310");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
    }

    @Test
    void T39_delete_存在しないbankCodeのとき404NotFoundとmessageを返す() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("振込手数料が見つかりません"))
                .when(transferFeeService).delete("9999");

        ResponseEntity<Map<String, Object>> result = controller.delete("9999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().get("message").toString().contains("振込手数料が見つかりません"));
    }

    @Test
    void T40_delete_サービス層で予期しない例外が発生したとき500を返す() {
        org.mockito.Mockito.doThrow(new RuntimeException("予期しないエラー"))
                .when(transferFeeService).delete(anyString());

        ResponseEntity<Map<String, Object>> result = controller.delete("0310");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().get("message"));
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private static TransferFeeResponse buildResponse() {
        TransferFeeResponse r = new TransferFeeResponse();
        r.setBankCode("0310");
        r.setTransferFee(200);
        r.setRemarks("GMOあおぞらネット銀行");
        return r;
    }

    private static TransferFeeRequest buildRequest() {
        TransferFeeRequest req = new TransferFeeRequest();
        req.setBankCode("0310");
        req.setTransferFee(200);
        req.setRemarks("GMOあおぞらネット銀行");
        return req;
    }

    private static HttpSession buildSession(String loginUserId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.LOGIN_USER, loginUserId);
        return session;
    }
}
