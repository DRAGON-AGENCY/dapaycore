package jp.co.dragonagency.dapaycore.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 運用管理ポータルへのアクセスに認証を要求するインターセプター。
 * セッションに社員番号が保持されていない場合は運用管理ログイン画面へリダイレクトする。
 */
public class OperationAuthInterceptor implements HandlerInterceptor {

    private static final String REDIRECT_LOGIN = "/login_operation.html";

    /**
     * リクエストの処理前に運用管理ログイン状態を確認する。
     * セッションに {@code loginUser} が存在しない場合は運用管理ログイン画面へリダイレクトして処理を中断する。
     *
     * @param request 受信した HTTP 要求
     * @param response 返却する HTTP 応答
     * @param handler 実行対象のハンドラ
     * @return 認証済みの場合は true。未認証の場合は false
     * @throws Exception 応答の書き込みで入出力例外が発生した場合
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(SessionAttributeNames.LOGIN_USER) != null) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + REDIRECT_LOGIN);
        return false;
    }
}
