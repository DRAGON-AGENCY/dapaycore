package jp.co.dragonagency.dapaycore.controller.api;

import jp.co.dragonagency.dapaycore.controller.NetStarsImportControlApiController;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * {@link NetStarsImportControlApiController} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の 項番 T43〜T45（■ テストケース一覧）に対応する。
 */
@ExtendWith(MockitoExtension.class)
class NetStarsImportControlApiControllerTest {

    @Mock
    private NetStarsSettlementImportService importService;

    @InjectMocks
    private NetStarsImportControlApiController controller;

    @Test
    void T43_stop_operatorの社員番号で停止し200OKを返す() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.LOGIN_USER, "user001");

        ResponseEntity<Map<String, Object>> result = controller.stop(session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(false, result.getBody().get("enabled"));
        verify(importService).stopImport("user001");
    }

    @Test
    void T44_restart_operatorの社員番号で再開し取込を非同期起動し200OKを返す() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.LOGIN_USER, "user002");

        ResponseEntity<Map<String, Object>> result = controller.restart(session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("enabled"));
        verify(importService).restartImport("user002");
        verify(importService).triggerRestartImport();
    }

    @Test
    void T45_stop_サービスで例外が発生したとき500を返す() {
        doThrow(new RuntimeException("失敗"))
                .when(importService).stopImport(nullable(String.class));

        ResponseEntity<Map<String, Object>> result =
                controller.stop(new MockHttpSession());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
