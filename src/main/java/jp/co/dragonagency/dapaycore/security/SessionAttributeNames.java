package jp.co.dragonagency.dapaycore.security;

/**
 * セッションに保持する属性名を定義する定数クラス。
 * ログイン中ユーザの user_id と権限コードを画面・処理間で共有する。
 */
public final class SessionAttributeNames {

    /** ログイン中ユーザの user_id を保持するセッション属性名。 */
    public static final String LOGIN_USER = "loginUser";

    /** ログイン中ユーザの権限コードを保持するセッション属性名。 */
    public static final String AUTHORITY_CODE = "authorityCode";

    private SessionAttributeNames() {
    }
}
