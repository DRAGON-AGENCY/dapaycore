package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MerchantLoginResponse;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 加盟店のログイン認証を担うサービス。
 * メールアドレスと仮／本パスワードを照合し、認証結果を返す。
 */
@Service
public class MerchantLoginService {

    private static final String ERROR_CREDENTIALS =
            "メールアドレスまたはパスワードが正しくありません。";

    private final MerchantApplicationRepository repository;
    private final PasswordEncoder passwordEncoder;

    public MerchantLoginService(
            MerchantApplicationRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * メールアドレスとパスワードで認証する。
     * 仮パスワードで一致した場合は本パスワード登録が必要なことを返す。
     *
     * @param email    メールアドレス
     * @param password 入力パスワード
     * @return 認証結果
     */
    public MerchantLoginResponse authenticate(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            return new MerchantLoginResponse(
                    false, "メールアドレスとパスワードを入力してください。", false, null);
        }

        Optional<MerchantApplication> opt = repository.findByContactEmail(email.trim());
        if (opt.isEmpty()) {
            return new MerchantLoginResponse(false, ERROR_CREDENTIALS, false, null);
        }

        MerchantApplication app = opt.get();

        if (app.isPasswordSetFlg()
                && app.getPasswordHash() != null
                && passwordEncoder.matches(password, app.getPasswordHash())) {
            return new MerchantLoginResponse(true, null, false, app.getMemberCode());
        }

        if (!app.isPasswordSetFlg()
                && app.getTempPasswordHash() != null
                && passwordEncoder.matches(password, app.getTempPasswordHash())) {
            return new MerchantLoginResponse(true, null, true, app.getMemberCode());
        }

        return new MerchantLoginResponse(false, ERROR_CREDENTIALS, false, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
