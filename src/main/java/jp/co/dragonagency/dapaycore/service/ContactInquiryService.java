package jp.co.dragonagency.dapaycore.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.dragonagency.dapaycore.dto.ContactInquiryRequest;
import jp.co.dragonagency.dapaycore.dto.ContactInquiryResponse;
import jp.co.dragonagency.dapaycore.model.ContactInquiry;
import jp.co.dragonagency.dapaycore.repository.ContactInquiryRepository;

/**
 * お問い合わせの送信・一覧取得を担うサービス。
 */
@Service
public class ContactInquiryService {

    private static final String INQUIRY_NUMBER_PREFIX = "INQ-";
    private static final int INQUIRY_NUMBER_DIGITS = 4;
    private static final int FIRST_INQUIRY_NUMBER = 1;

    private static final int SUBJECT_MAX_LENGTH = 100;
    private static final int BODY_MAX_LENGTH = 1_000;

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "契約内容について",
            "請求・精算について",
            "端末・機器について",
            "セキュリティについて",
            "その他");

    private static final String MESSAGE_INVALID_INPUT =
            "入力内容が正しくありません。";
    private static final String MESSAGE_CATEGORY_REQUIRED =
            "カテゴリを選択してください。";
    private static final String MESSAGE_CATEGORY_INVALID =
            "選択されたカテゴリが正しくありません。";
    private static final String MESSAGE_SUBJECT_REQUIRED =
            "件名を入力してください。";
    private static final String MESSAGE_SUBJECT_TOO_LONG =
            "件名は 100 文字以内で入力してください。";
    private static final String MESSAGE_BODY_REQUIRED =
            "お問い合わせ内容を入力してください。";
    private static final String MESSAGE_BODY_TOO_LONG =
            "お問い合わせ内容は 1000 文字以内で入力してください。";

    private final ContactInquiryRepository contactInquiryRepository;

    public ContactInquiryService(
            ContactInquiryRepository contactInquiryRepository) {
        this.contactInquiryRepository = contactInquiryRepository;
    }

    /**
     * 全お問い合わせを受付日時の降順で取得する。
     *
     * @return お問い合わせの一覧
     */
    public List<ContactInquiry> findAllInquiries() {
        return contactInquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * お問い合わせを送信（新規登録）する。
     * 入力値を検査し、問題があれば失敗結果を返す。
     *
     * @param request 画面から送信されたお問い合わせ内容
     * @return 処理結果
     */
    @Transactional
    public ContactInquiryResponse submitInquiry(ContactInquiryRequest request) {
        if (request == null) {
            return new ContactInquiryResponse(false, MESSAGE_INVALID_INPUT);
        }

        String category = trimToEmpty(request.getCategory());
        String subject = trimToEmpty(request.getSubject());
        String body = trimToEmpty(request.getBody());

        String validationMessage = validateInput(category, subject, body);
        if (validationMessage != null) {
            return new ContactInquiryResponse(false, validationMessage);
        }

        ContactInquiry inquiry = new ContactInquiry();
        inquiry.setInquiryNumber(generateNextInquiryNumber());
        inquiry.setCategory(category);
        inquiry.setSubject(subject);
        inquiry.setBody(body);
        inquiry.setStatus(ContactInquiry.STATUS_RECEIVED);

        LocalDateTime now = LocalDateTime.now();
        inquiry.setCreatedAt(now);
        inquiry.setUpdatedAt(now);

        contactInquiryRepository.save(inquiry);
        return new ContactInquiryResponse(true, null);
    }

    /**
     * 入力値の必須・形式・長さを検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(
            String category, String subject, String body) {
        if (category.isEmpty()) {
            return MESSAGE_CATEGORY_REQUIRED;
        }
        if (!VALID_CATEGORIES.contains(category)) {
            return MESSAGE_CATEGORY_INVALID;
        }
        if (subject.isEmpty()) {
            return MESSAGE_SUBJECT_REQUIRED;
        }
        if (subject.length() > SUBJECT_MAX_LENGTH) {
            return MESSAGE_SUBJECT_TOO_LONG;
        }
        if (body.isEmpty()) {
            return MESSAGE_BODY_REQUIRED;
        }
        if (body.length() > BODY_MAX_LENGTH) {
            return MESSAGE_BODY_TOO_LONG;
        }
        return null;
    }

    /**
     * 既存の最大お問い合わせ番号を基に、次の番号を採番する。
     * 形式は「INQ-」+ 0 埋め 4 桁の連番（例: INQ-0001）。
     *
     * @return 新しいお問い合わせ番号
     */
    private String generateNextInquiryNumber() {
        Optional<ContactInquiry> latest =
                contactInquiryRepository.findFirstByOrderByInquiryNumberDesc();
        int nextNumber = FIRST_INQUIRY_NUMBER;
        if (latest.isPresent()) {
            nextNumber =
                    extractInquiryNumber(latest.get().getInquiryNumber()) + 1;
        }
        return String.format(
                "%s%0" + INQUIRY_NUMBER_DIGITS + "d",
                INQUIRY_NUMBER_PREFIX, nextNumber);
    }

    /**
     * お問い合わせ番号から末尾の数値部分を取り出す。
     * 数値として解釈できない場合は 0 を返す。
     *
     * @param inquiryNumber お問い合わせ番号
     * @return 数値部分
     */
    private int extractInquiryNumber(String inquiryNumber) {
        String digits = inquiryNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

    /**
     * 文字列の前後の空白を除去する。null の場合は空文字を返す。
     *
     * @param value 対象の文字列
     * @return 前後の空白を除去した文字列
     */
    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
