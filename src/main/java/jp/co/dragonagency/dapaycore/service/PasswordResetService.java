package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * パスワード再設定（仮パスワード再発行）を担うサービス。
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MerchantApplicationRepository merchantApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public PasswordResetService(
            MerchantApplicationRepository merchantApplicationRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService) {
        this.merchantApplicationRepository = merchantApplicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    /**
     * 登録済みメールアドレスに仮パスワードを再発行してメール送信する。
     *
     * @param email 登録済みメールアドレス
     * @return メールアドレスが登録されており処理を実行した場合 true、未登録の場合 false
     */
    @Transactional
    public boolean requestReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        List<MerchantApplication> apps =
                merchantApplicationRepository.findByContactEmailAndDeleteFlagFalse(email.trim());
        if (apps.isEmpty()) {
            log.info("パスワード再設定リクエスト: 対象なし email={}", email.trim());
            return false;
        }

        MerchantApplication app = apps.get(0);
        String newTempPassword = generateTempPassword();

        app.setTempPasswordHash(passwordEncoder.encode(newTempPassword));
        app.setPasswordSetFlg(false);
        app.setUpdatedAt(LocalDateTime.now());
        merchantApplicationRepository.save(app);

        log.info("仮パスワード再発行: memberCode={}", app.getMemberCode());

        try {
            mailService.sendPasswordResetMail(
                    app.getContactEmail(),
                    app.getCorporateName(),
                    app.getMemberCode(),
                    newTempPassword);
        } catch (Exception e) {
            log.error("パスワード再設定メール送信失敗: memberCode={}", app.getMemberCode(), e);
        }
        return true;
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(
                    SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
