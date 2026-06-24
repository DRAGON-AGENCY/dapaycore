package jp.co.dragonagency.dapaycore.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 申込・照会ポータルへのアクセスに認証を要求するインターセプター。
 * セッションに会員コードが保持されていない場合は加盟店ログイン画面へリダイレクトする。
 */
public class MerchantAuthInterceptor implements HandlerInterceptor {

    private static final String REDIRECT_LOGIN = "/login.html";

    /**
     * リクエストの処理前に加盟店ログイン状態を確認する。
     * セッションに {@code merchantMemberCode} が存在しない場合は加盟店ログイン画面へリダイレクトして処理を中断する。
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
                && session.getAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE) != null) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + REDIRECT_LOGIN);
        return false;
    }
}
