package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.PasswordSetupRequest;
import jp.co.dragonagency.dapaycore.dto.PasswordSetupResponse;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 本パスワード登録を担うサービス。
 */
@Service
public class PasswordSetupService {

    private static final Logger log = LoggerFactory.getLogger(PasswordSetupService.class);

    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).{" + PASSWORD_MIN_LENGTH + ",}$");

    private final MerchantApplicationRepository merchantApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordSetupService(
            MerchantApplicationRepository merchantApplicationRepository,
            PasswordEncoder passwordEncoder) {
        this.merchantApplicationRepository = merchantApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 本パスワードを登録する。
     * 仮パスワードを照合し、問題なければ本パスワードのハッシュを保存する。
     *
     * @param request リクエスト
     * @return 処理結果
     */
    @Transactional
    public PasswordSetupResponse setup(PasswordSetupRequest request) {
        if (request == null
                || isBlank(request.getMemberCode())
                || isBlank(request.getTempPassword())
                || isBlank(request.getNewPassword())
                || isBlank(request.getConfirmPassword())) {
            return new PasswordSetupResponse(false, "入力内容に不備があります。");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return new PasswordSetupResponse(false, "新しいパスワードと確認用パスワードが一致しません。");
        }

        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            return new PasswordSetupResponse(false,
                    "パスワードは8文字以上で、英字と数字をそれぞれ1文字以上含めてください。");
        }

        Optional<MerchantApplication> opt =
                merchantApplicationRepository.findById(request.getMemberCode().trim());
        if (opt.isEmpty()) {
            return new PasswordSetupResponse(false, "会員コードまたはパスワードが正しくありません。");
        }

        MerchantApplication app = opt.get();

        if (app.isPasswordSetFlg()) {
            return new PasswordSetupResponse(false,
                    "本パスワードはすでに登録済みです。ログイン画面からログインしてください。");
        }

        if (app.getTempPasswordHash() == null
                || !passwordEncoder.matches(request.getTempPassword(), app.getTempPasswordHash())) {
            return new PasswordSetupResponse(false, "会員コードまたはパスワードが正しくありません。");
        }

        app.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        app.setTempPasswordHash(null);
        app.setPasswordSetFlg(true);
        app.setUpdatedAt(LocalDateTime.now());
        merchantApplicationRepository.save(app);

        log.info("本パスワードを登録しました: memberCode={}", app.getMemberCode());
        return new PasswordSetupResponse(true, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
