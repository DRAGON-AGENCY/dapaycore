package jp.co.dragonagency.dapaycore.advice;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CsrfTokenControllerAdvice の補助単体テスト。
 * 仕様書の項番には対応しない。項番82（CsrfMetaTagRenderingTest）の
 * 基盤となる Java ロジックを個別に検証する。
 */
@ExtendWith(MockitoExtension.class)
class CsrfTokenControllerAdviceTest {

    private final CsrfTokenControllerAdvice advice = new CsrfTokenControllerAdvice();

    // =========================================================
    // セッションにトークンあり
    // =========================================================

    @Test
    void csrfToken_セッションに既存トークンがあるとき同じ値を返す() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn("existing-token-abc");

        String result = advice.csrfToken(session);

        assertEquals("existing-token-abc", result);
    }

    @Test
    void csrfToken_セッションに既存トークンがあるとき新規トークンを生成しない() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn("existing-token-abc");

        advice.csrfToken(session);

        verify(session, never()).setAttribute(eq("csrfToken"), any());
    }

    // =========================================================
    // セッションにトークンなし
    // =========================================================

    @Test
    void csrfToken_セッションにトークンがないとき非空の文字列を返す() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn(null);

        String result = advice.csrfToken(session);

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void csrfToken_セッションにトークンがないとき生成したトークンをセッションに保存する() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn(null);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        String result = advice.csrfToken(session);

        verify(session).setAttribute(eq("csrfToken"), captor.capture());
        assertEquals(result, captor.getValue());
    }

    @Test
    void csrfToken_生成されたトークンはBase64URL文字のみで構成される() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn(null);

        String result = advice.csrfToken(session);

        // Base64URL（パディングなし）は A-Za-z0-9_- のみ
        assertTrue(result.matches("[A-Za-z0-9_-]+"),
                "トークンに Base64URL 以外の文字が含まれています: " + result);
    }

    @Test
    void csrfToken_生成されたトークンは32バイト由来の43文字() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("csrfToken")).thenReturn(null);

        String result = advice.csrfToken(session);

        // 32 バイト → Base64URL（パディングなし）= ceil(32 * 4 / 3) = 43 文字
        assertEquals(43, result.length(),
                "トークンの長さが想定（43文字）と異なります: " + result.length());
    }
}
