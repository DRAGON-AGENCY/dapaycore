package jp.co.dragonagency.dapaycore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * メール送信を担うサービス。
 * テキストテンプレートファイルを読み込み、変数を置換してメールを送信する。
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String TEMPLATE_PATH = "templates/mail/registration.txt";
    private static final String TEMPLATE_PATH_RESET = "templates/mail/password_reset.txt";
    private static final String SUBJECT_REGISTRATION =
            "【mPOSなび】加盟店申込受付完了のご案内";
    private static final String SUBJECT_PASSWORD_RESET =
            "【mPOSなび】パスワード再設定のご案内";

    private final JavaMailSender mailSender;

    @Value("${mail.from:noreply@dragonagency.co.jp}")
    private String fromAddress;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 加盟店申込受付完了メールを送信する。
     *
     * @param to            宛先メールアドレス
     * @param corporateName 法人名
     * @param memberCode    発行された会員コード
     * @param tempPassword  仮パスワード（平文）
     */
    public void sendRegistrationMail(
            String to,
            String corporateName,
            String memberCode,
            String tempPassword) throws IOException {
        String body = buildMailBody(corporateName, memberCode, tempPassword);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(SUBJECT_REGISTRATION);
        message.setText(body);

        mailSender.send(message);
        log.info("登録完了メール送信: to={}, memberCode={}", to, memberCode);
    }

    /**
     * パスワード再設定（仮パスワード再発行）メールを送信する。
     *
     * @param to             宛先メールアドレス
     * @param corporateName  法人名
     * @param memberCode     会員コード
     * @param newTempPassword 再発行した仮パスワード（平文）
     */
    public void sendPasswordResetMail(
            String to,
            String corporateName,
            String memberCode,
            String newTempPassword) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH_RESET);
        if (is == null) {
            throw new IOException("メールテンプレートが見つかりません: " + TEMPLATE_PATH_RESET);
        }
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String body = template
                .replace("[(${corporateName})]", corporateName != null ? corporateName : "")
                .replace("[(${memberCode})]", memberCode != null ? memberCode : "")
                .replace("[(${newTempPassword})]", newTempPassword != null ? newTempPassword : "");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(SUBJECT_PASSWORD_RESET);
        message.setText(body);

        mailSender.send(message);
        log.info("パスワード再設定メール送信: to={}, memberCode={}", to, memberCode);
    }

    private String buildMailBody(
            String corporateName,
            String memberCode,
            String tempPassword) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IOException("メールテンプレートが見つかりません: " + TEMPLATE_PATH);
        }
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return template
                .replace("[(${corporateName})]", corporateName != null ? corporateName : "")
                .replace("[(${memberCode})]", memberCode != null ? memberCode : "")
                .replace("[(${tempPassword})]", tempPassword != null ? tempPassword : "");
    }
}
