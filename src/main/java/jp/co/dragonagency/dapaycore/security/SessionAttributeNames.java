package jp.co.dragonagency.dapaycore.security;

/**
 * セッションに保持する属性名を定義する定数クラス。
 * ログイン中ユーザの社員番号と権限コードを画面・処理間で共有する。
 */
public final class SessionAttributeNames {

    /** ログイン中ユーザの社員番号を保持するセッション属性名。 */
    public static final String LOGIN_USER = "loginUser";

    /** ログイン中ユーザの権限コードを保持するセッション属性名。 */
    public static final String AUTHORITY_CODE = "authorityCode";

    /** ログイン中の加盟店会員コードを保持するセッション属性名。 */
    public static final String MERCHANT_MEMBER_CODE = "merchantMemberCode";

    private SessionAttributeNames() {
    }
}
