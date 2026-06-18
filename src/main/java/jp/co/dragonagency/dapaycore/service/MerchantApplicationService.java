package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MerchantApplicationRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantApplicationResponse;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 加盟店申込の登録を担うサービス。
 */
@Service
public class MerchantApplicationService {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApplicationService.class);

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MerchantApplicationRepository merchantApplicationRepository;
    private final MerchantApplicationDocumentRepository documentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public MerchantApplicationService(
            MerchantApplicationRepository merchantApplicationRepository,
            MerchantApplicationDocumentRepository documentRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        this.merchantApplicationRepository = merchantApplicationRepository;
        this.documentRepository = documentRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 加盟店申込を登録し、書類ファイルを保存する。
     *
     * @param request         入力データ
     * @param businessPermits 営業許可関連書類（複数可）
     * @param idDocFront      本人確認書類（表面）
     * @param idDocBack       本人確認書類（裏面）
     * @param openingPlans    開業届・店舗図面（複数可）
     * @param productMaterials 商材がわかる資料（複数可）
     * @param eventVenues     イベント会場図面（複数可）
     * @return 処理結果
     */
    @Transactional
    public MerchantApplicationResponse register(
            MerchantApplicationRequest request,
            List<MultipartFile> businessPermits,
            MultipartFile idDocFront,
            MultipartFile idDocBack,
            List<MultipartFile> openingPlans,
            List<MultipartFile> productMaterials,
            List<MultipartFile> eventVenues) {

        if (request == null) {
            return new MerchantApplicationResponse(false, null, null, "リクエストが不正です。");
        }

        validateRequest(request);

        String memberCode = generateNextMemberCode();
        String tempPassword = generateTempPassword();
        LocalDateTime now = LocalDateTime.now();

        MerchantApplication application = buildApplication(request, memberCode, now);
        application.setTempPasswordHash(passwordEncoder.encode(tempPassword));
        merchantApplicationRepository.save(application);

        List<MerchantApplicationDocument> docs = new ArrayList<>();
        collectDocumentFiles(docs, memberCode, businessPermits,
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, now);
        collectDocumentFile(docs, memberCode, idDocFront,
                MerchantApplicationDocument.TYPE_ID_FRONT, now);
        collectDocumentFile(docs, memberCode, idDocBack,
                MerchantApplicationDocument.TYPE_ID_BACK, now);
        collectDocumentFiles(docs, memberCode, openingPlans,
                MerchantApplicationDocument.TYPE_OPENING_PLAN, now);
        collectDocumentFiles(docs, memberCode, productMaterials,
                MerchantApplicationDocument.TYPE_PRODUCT_MATERIAL, now);
        collectDocumentFiles(docs, memberCode, eventVenues,
                MerchantApplicationDocument.TYPE_EVENT_VENUE, now);
        saveDocumentsBatch(docs);

        log.info("加盟店申込を登録しました: memberCode={} 書類{}件", memberCode, docs.size());
        return new MerchantApplicationResponse(true, memberCode, tempPassword, null);
    }

    /**
     * 申込リクエストの入力値を検証する。
     * 不正な値がある場合は IllegalArgumentException をスローする。
     */
    private void validateRequest(MerchantApplicationRequest req) {
        List<String> errors = new ArrayList<>();

        // STEP 1: 全規約同意が必須
        if (!req.isAgreedStarpay()) errors.add("StarPay決済サービス加盟店規約への確認が必要です");
        if (!req.isAgreedJcb()) errors.add("JCB加盟店規約・加盟店特約への確認が必要です");
        if (!req.isAgreedRyuginVisaMcCu()) errors.add("琉球銀行加盟店規約（Visa/Mastercard/銀聯）への確認が必要です");
        if (!req.isAgreedRyuginCuQr()) errors.add("銀聯QRコード決済サービス利用加盟店規約への確認が必要です");
        if (!req.isAgreedAgencyDelegation()) errors.add("代理申請の委任への同意が必要です");
        if (!req.isAgreedServiceTerms()) errors.add("当社サービス利用規約への同意が必要です");
        if (!req.isAgreedPrivacyPolicy()) errors.add("当社プライバシーポリシーへの同意が必要です");
        if (!req.isAgreedAuthorityConfirmed()) errors.add("代表者・契約締結権限の確認が必要です");

        // STEP 2: 取引形態
        boolean txAny = req.isTxTypeVisitSales() || req.isTxTypeContinuousService()
                || req.isTxTypePhoneSolicitation() || req.isTxTypePrepaidService()
                || req.isTxTypeBusinessInduction() || req.isTxTypeChainSales()
                || req.isTxTypeNoneApplicable();
        if (!txAny) errors.add("取引形態を1つ以上選択してください");
        requireField(req.getBusinessEntityType(), "法人区分", errors);
        requireField(req.getSalesFormat(), "販売形態", errors);
        requireField(req.getOperationFormat(), "運営形態", errors);

        // STEP 3: 決済種類
        boolean qrAny = req.isPayQrWechatPay() || req.isPayQrPaypay() || req.isPayQrDBarai()
                || req.isPayQrAuPay() || req.isPayQrMerpay() || req.isPayQrRakutenPay()
                || req.isPayQrAlipayPlus() || req.isPayQrJkoPay();
        if (!qrAny) errors.add("QRコード決済を1つ以上選択してください");
        boolean creditAny = req.isPayCreditJcb() || req.isPayCreditVisa()
                || req.isPayCreditMastercard() || req.isPayCreditDiscover()
                || req.isPayCreditDiners() || req.isPayCreditAmex();
        if (!creditAny) errors.add("クレジットカード決済を1つ以上選択してください");
        boolean emoneyAny = req.isPayEmoneyId() || req.isPayEmoneyWaon()
                || req.isPayEmoneyRakutenEdy() || req.isPayEmoneyNanaco()
                || req.isPayEmoneyTransitIc();
        if (!emoneyAny) errors.add("電子マネー決済を1つ以上選択してください");

        // STEP 4: 申込者情報
        requireField(req.getCorporateName(), "法人名", errors);
        requireField(req.getCorporateNameKana(), "法人名（カナ）", errors);
        requireField(req.getCorporateNameEn(), "法人名（英語）", errors);
        requireField(req.getBrandName(), "ブランド名（屋号）", errors);
        requireField(req.getBrandNameKana(), "ブランド名（カナ）", errors);
        requireField(req.getBrandNameEn(), "ブランド名（英語）", errors);
        requireField(req.getAnnualRevenue(), "年商", errors);
        requireField(req.getIndustryCategory(), "業種（カテゴリー）", errors);
        requireField(req.getIndustryDetail(), "業種（詳細）", errors);
        requireField(req.getBusinessDescription(), "事業内容及び取扱商材", errors);
        // STEP 4: 代表者情報
        requireField(req.getRepLastName(), "代表者 姓", errors);
        requireField(req.getRepLastNameKana(), "代表者 姓（カナ）", errors);
        requireField(req.getRepLastNameEn(), "代表者 姓（英語）", errors);
        requireField(req.getRepFirstName(), "代表者 名", errors);
        requireField(req.getRepFirstNameKana(), "代表者 名（カナ）", errors);
        requireField(req.getRepFirstNameEn(), "代表者 名（英語）", errors);
        requireField(req.getRepBirthDate(), "代表者 生年月日", errors);
        requireField(req.getRepGender(), "代表者 性別", errors);
        requireField(req.getRepZipCode(), "代表者 郵便番号", errors);
        requireField(req.getRepPrefecture(), "代表者 都道府県", errors);
        requireField(req.getRepCity(), "代表者 市区町村", errors);
        requireField(req.getRepTown(), "代表者 町域", errors);
        requireField(req.getRepStreetNumber(), "代表者 番地", errors);
        requireField(req.getRepPhone(), "代表者 電話番号", errors);
        // STEP 4: 担当者情報
        requireField(req.getContactLastName(), "担当者 姓", errors);
        requireField(req.getContactLastNameKana(), "担当者 姓（カナ）", errors);
        requireField(req.getContactFirstName(), "担当者 名", errors);
        requireField(req.getContactFirstNameKana(), "担当者 名（カナ）", errors);
        requireField(req.getContactDepartment(), "担当者 部署名", errors);
        requireField(req.getContactEmail(), "担当者 メールアドレス", errors);
        requireField(req.getContactPhone1(), "担当者 電話番号", errors);
        requireField(req.getContactZipCode(), "担当者 郵便番号", errors);
        requireField(req.getContactPrefecture(), "担当者 都道府県", errors);
        requireField(req.getContactCity(), "担当者 市区町村", errors);
        requireField(req.getContactTown(), "担当者 町域", errors);
        requireField(req.getContactStreetNumber(), "担当者 番地", errors);

        // STEP 4: 法人番号の桁数（DBカラム VARCHAR(13) に対応）
        if (req.getCorporateNumber() != null && !req.getCorporateNumber().trim().isEmpty()
                && req.getCorporateNumber().trim().length() > 13) {
            errors.add("法人番号は13桁以内で入力してください");
        }

        // STEP 5: 口座情報
        requireField(req.getBankCode(), "金融機関コード", errors);
        requireField(req.getBankName(), "金融機関名", errors);
        requireField(req.getBranchCode(), "支店コード", errors);
        requireField(req.getBranchName(), "支店名", errors);
        requireField(req.getAccountType(), "預金種別", errors);
        requireField(req.getAccountNumber(), "口座番号", errors);
        requireField(req.getAccountHolderKana(), "口座名義（カナ）", errors);
        // 口座番号は日本の標準 7 桁（DBカラム VARCHAR(7) に対応）
        if (req.getAccountNumber() != null && !req.getAccountNumber().trim().isEmpty()
                && req.getAccountNumber().trim().length() > 7) {
            errors.add("口座番号は7桁以内で入力してください");
        }

        // STEP 6: 店舗情報
        requireField(req.getStoreName(), "店舗名", errors);
        requireField(req.getStoreNameKana(), "店舗名（カナ）", errors);
        requireField(req.getStoreNameEn(), "店舗名（英語）", errors);
        requireField(req.getStoreBrandName(), "店舗ブランド名", errors);
        requireField(req.getStoreBrandNameKana(), "店舗ブランド名（カナ）", errors);
        requireField(req.getStoreBrandNameEn(), "店舗ブランド名（英語）", errors);
        requireField(req.getStoreIndustryCategory(), "店舗業種（カテゴリ）", errors);
        requireField(req.getStoreIndustryDetail(), "店舗業種（詳細）", errors);
        requireField(req.getStoreProductDescription(), "店舗商材の詳細", errors);
        Integer storeCount = parseInt(req.getStoreCount());
        if (storeCount == null || storeCount < 1) errors.add("店舗数は1以上を入力してください");
        Integer storeAvgPrice = parseInt(req.getStoreAveragePrice());
        if (storeAvgPrice == null || storeAvgPrice < 0) errors.add("平均単価は0以上を入力してください");
        requireField(req.getStoreBankAccount(), "店舗口座", errors);
        requireField(req.getStoreReceiptName(), "レシート名", errors);
        requireField(req.getShopZipCode(), "店舗 郵便番号", errors);
        requireField(req.getShopPrefecture(), "店舗 都道府県", errors);
        requireField(req.getShopCity(), "店舗 市区町村", errors);
        requireField(req.getShopTown(), "店舗 町域", errors);
        requireField(req.getShopStreetNumber(), "店舗 番地", errors);
        requireField(req.getShopPhone(), "店舗 電話番号", errors);
        requireField(req.getTerminalPossessionStatus(), "端末保持状況", errors);

        // STEP 8: 発送情報
        requireField(req.getDeliveryZipCode(), "お届け先 郵便番号", errors);
        requireField(req.getDeliveryPrefecture(), "お届け先 都道府県", errors);
        requireField(req.getDeliveryCity(), "お届け先 市区町村", errors);
        requireField(req.getDeliveryTown(), "お届け先 町域", errors);
        requireField(req.getDeliveryStreetNumber(), "お届け先 番地", errors);
        requireField(req.getDeliveryPhone(), "お届け先 電話番号", errors);
        requireField(req.getDeliveryReceiver(), "受取人", errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" / ", errors));
        }
    }

    private void requireField(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + "は必須です");
        }
    }

    private MerchantApplication buildApplication(
            MerchantApplicationRequest req,
            String memberCode,
            LocalDateTime now) {

        MerchantApplication app = new MerchantApplication();
        app.setMemberCode(memberCode);
        app.setApplicationStatus(MerchantApplication.STATUS_UNREVIEWED);
        app.setSubmittedAt(now);
        app.setCreatedAt(now);
        app.setUpdatedAt(now);
        applyStep1(app, req);
        applyStep2(app, req);
        applyStep3(app, req);
        applyStep4(app, req);
        applyStep5(app, req);
        applyStep6(app, req);
        applyStep8(app, req);
        return app;
    }

    private void applyStep1(MerchantApplication app, MerchantApplicationRequest req) {
        app.setAgreedStarpay(req.isAgreedStarpay());
        app.setAgreedJcb(req.isAgreedJcb());
        app.setAgreedRyuginVisaMcCu(req.isAgreedRyuginVisaMcCu());
        app.setAgreedRyuginCuQr(req.isAgreedRyuginCuQr());
        app.setAgreedAgencyDelegation(req.isAgreedAgencyDelegation());
        app.setAgreedServiceTerms(req.isAgreedServiceTerms());
        app.setAgreedPrivacyPolicy(req.isAgreedPrivacyPolicy());
        app.setAgreedAuthorityConfirmed(req.isAgreedAuthorityConfirmed());
    }

    private void applyStep2(MerchantApplication app, MerchantApplicationRequest req) {
        app.setTxTypeVisitSales(req.isTxTypeVisitSales());
        app.setTxTypeContinuousService(req.isTxTypeContinuousService());
        app.setTxTypePhoneSolicitation(req.isTxTypePhoneSolicitation());
        app.setTxTypePrepaidService(req.isTxTypePrepaidService());
        app.setTxTypeBusinessInduction(req.isTxTypeBusinessInduction());
        app.setTxTypeChainSales(req.isTxTypeChainSales());
        app.setTxTypeNoneApplicable(req.isTxTypeNoneApplicable());
        app.setBusinessEntityType(nullIfEmpty(req.getBusinessEntityType()));
        app.setSalesFormat(nullIfEmpty(req.getSalesFormat()));
        app.setOperationFormat(nullIfEmpty(req.getOperationFormat()));
    }

    private void applyStep3(MerchantApplication app, MerchantApplicationRequest req) {
        app.setPayQrWechatPay(req.isPayQrWechatPay());
        app.setPayQrPaypay(req.isPayQrPaypay());
        app.setPayQrDBarai(req.isPayQrDBarai());
        app.setPayQrAuPay(req.isPayQrAuPay());
        app.setPayQrMerpay(req.isPayQrMerpay());
        app.setPayQrRakutenPay(req.isPayQrRakutenPay());
        app.setPayQrAlipayPlus(req.isPayQrAlipayPlus());
        app.setPayQrJkoPay(req.isPayQrJkoPay());
        app.setPayCreditJcb(req.isPayCreditJcb());
        app.setPayCreditDiscover(req.isPayCreditDiscover());
        app.setPayCreditVisa(req.isPayCreditVisa());
        app.setPayCreditMastercard(req.isPayCreditMastercard());
        app.setPayCreditDiners(req.isPayCreditDiners());
        app.setPayCreditAmex(req.isPayCreditAmex());
        app.setPayCreditBonus(nullIfEmpty(req.getPayCreditBonus()));
        app.setPayCreditTwoTimes(nullIfEmpty(req.getPayCreditTwoTimes()));
        app.setPayCreditInstallment(nullIfEmpty(req.getPayCreditInstallment()));
        app.setPayCreditRevolving(nullIfEmpty(req.getPayCreditRevolving()));
        app.setPayEmoneyId(req.isPayEmoneyId());
        app.setPayEmoneyWaon(req.isPayEmoneyWaon());
        app.setPayEmoneyRakutenEdy(req.isPayEmoneyRakutenEdy());
        app.setPayEmoneyNanaco(req.isPayEmoneyNanaco());
        app.setPayEmoneyTransitIc(req.isPayEmoneyTransitIc());
    }

    private void applyStep4(MerchantApplication app, MerchantApplicationRequest req) {
        app.setCorporateNumber(nullIfEmpty(req.getCorporateNumber()));
        app.setCorporateName(nullIfEmpty(req.getCorporateName()));
        app.setCorporateNameKana(nullIfEmpty(req.getCorporateNameKana()));
        app.setCorporateNameEn(nullIfEmpty(req.getCorporateNameEn()));
        app.setEstablishmentDate(parseDate(req.getEstablishmentDate()));
        app.setCorporateType(nullIfEmpty(req.getCorporateType()));
        app.setBrandName(nullIfEmpty(req.getBrandName()));
        app.setBrandNameKana(nullIfEmpty(req.getBrandNameKana()));
        app.setBrandNameEn(nullIfEmpty(req.getBrandNameEn()));
        app.setCompanyUrl(nullIfEmpty(req.getCompanyUrl()));
        app.setAnnualRevenue(parseLong(req.getAnnualRevenue()));
        app.setCapitalAmount(parseLong(req.getCapitalAmount()));
        app.setEmployeeCount(parseInt(req.getEmployeeCount()));
        app.setIndustryCategory(nullIfEmpty(req.getIndustryCategory()));
        app.setIndustryDetail(nullIfEmpty(req.getIndustryDetail()));
        app.setBusinessDescription(nullIfEmpty(req.getBusinessDescription()));
        app.setCompanyZipCode(nullIfEmpty(req.getCompanyZipCode()));
        app.setCompanyPrefecture(nullIfEmpty(req.getCompanyPrefecture()));
        app.setCompanyPrefectureKana(nullIfEmpty(req.getCompanyPrefectureKana()));
        app.setCompanyCity(nullIfEmpty(req.getCompanyCity()));
        app.setCompanyCityKana(nullIfEmpty(req.getCompanyCityKana()));
        app.setCompanyTown(nullIfEmpty(req.getCompanyTown()));
        app.setCompanyTownKana(nullIfEmpty(req.getCompanyTownKana()));
        app.setCompanyStreetNumber(nullIfEmpty(req.getCompanyStreetNumber()));
        app.setCompanyStreetNumberKana(nullIfEmpty(req.getCompanyStreetNumberKana()));
        app.setCompanyBuilding(nullIfEmpty(req.getCompanyBuilding()));
        app.setCompanyBuildingKana(nullIfEmpty(req.getCompanyBuildingKana()));
        app.setCompanyPhone(nullIfEmpty(req.getCompanyPhone()));
        app.setCompanyFax(nullIfEmpty(req.getCompanyFax()));
        app.setCompanyMobile(nullIfEmpty(req.getCompanyMobile()));
        app.setRepLastName(nullIfEmpty(req.getRepLastName()));
        app.setRepLastNameKana(nullIfEmpty(req.getRepLastNameKana()));
        app.setRepLastNameEn(nullIfEmpty(req.getRepLastNameEn()));
        app.setRepFirstName(nullIfEmpty(req.getRepFirstName()));
        app.setRepFirstNameKana(nullIfEmpty(req.getRepFirstNameKana()));
        app.setRepFirstNameEn(nullIfEmpty(req.getRepFirstNameEn()));
        app.setRepBirthDate(parseDate(req.getRepBirthDate()));
        app.setRepGender(nullIfEmpty(req.getRepGender()));
        app.setRepZipCode(nullIfEmpty(req.getRepZipCode()));
        app.setRepPrefecture(nullIfEmpty(req.getRepPrefecture()));
        app.setRepPrefectureKana(nullIfEmpty(req.getRepPrefectureKana()));
        app.setRepCity(nullIfEmpty(req.getRepCity()));
        app.setRepCityKana(nullIfEmpty(req.getRepCityKana()));
        app.setRepTown(nullIfEmpty(req.getRepTown()));
        app.setRepTownKana(nullIfEmpty(req.getRepTownKana()));
        app.setRepStreetNumber(nullIfEmpty(req.getRepStreetNumber()));
        app.setRepStreetNumberKana(nullIfEmpty(req.getRepStreetNumberKana()));
        app.setRepBuilding(nullIfEmpty(req.getRepBuilding()));
        app.setRepPhone(nullIfEmpty(req.getRepPhone()));
        app.setContactLastName(nullIfEmpty(req.getContactLastName()));
        app.setContactLastNameKana(nullIfEmpty(req.getContactLastNameKana()));
        app.setContactFirstName(nullIfEmpty(req.getContactFirstName()));
        app.setContactFirstNameKana(nullIfEmpty(req.getContactFirstNameKana()));
        app.setContactZipCode(nullIfEmpty(req.getContactZipCode()));
        app.setContactPrefecture(nullIfEmpty(req.getContactPrefecture()));
        app.setContactPrefectureKana(nullIfEmpty(req.getContactPrefectureKana()));
        app.setContactCity(nullIfEmpty(req.getContactCity()));
        app.setContactCityKana(nullIfEmpty(req.getContactCityKana()));
        app.setContactTown(nullIfEmpty(req.getContactTown()));
        app.setContactTownKana(nullIfEmpty(req.getContactTownKana()));
        app.setContactStreetNumber(nullIfEmpty(req.getContactStreetNumber()));
        app.setContactStreetNumberKana(nullIfEmpty(req.getContactStreetNumberKana()));
        app.setContactBuilding(nullIfEmpty(req.getContactBuilding()));
        app.setContactBuildingKana(nullIfEmpty(req.getContactBuildingKana()));
        app.setContactDepartment(nullIfEmpty(req.getContactDepartment()));
        app.setContactEmail(nullIfEmpty(req.getContactEmail()));
        app.setContactPhone1(nullIfEmpty(req.getContactPhone1()));
        app.setContactPhone2(nullIfEmpty(req.getContactPhone2()));
    }

    private void applyStep5(MerchantApplication app, MerchantApplicationRequest req) {
        app.setBankCode(nullIfEmpty(req.getBankCode()));
        app.setBankName(nullIfEmpty(req.getBankName()));
        app.setBranchCode(nullIfEmpty(req.getBranchCode()));
        app.setBranchName(nullIfEmpty(req.getBranchName()));
        app.setAccountType(nullIfEmpty(req.getAccountType()));
        app.setAccountNumber(nullIfEmpty(req.getAccountNumber()));
        app.setAccountHolderKana(nullIfEmpty(req.getAccountHolderKana()));
    }

    private void applyStep6(MerchantApplication app, MerchantApplicationRequest req) {
        app.setStoreName(nullIfEmpty(req.getStoreName()));
        app.setStoreNameKana(nullIfEmpty(req.getStoreNameKana()));
        app.setStoreNameEn(nullIfEmpty(req.getStoreNameEn()));
        app.setStoreBrandName(nullIfEmpty(req.getStoreBrandName()));
        app.setStoreBrandNameKana(nullIfEmpty(req.getStoreBrandNameKana()));
        app.setStoreBrandNameEn(nullIfEmpty(req.getStoreBrandNameEn()));
        app.setStoreIndustryCategory(nullIfEmpty(req.getStoreIndustryCategory()));
        app.setStoreIndustryDetail(nullIfEmpty(req.getStoreIndustryDetail()));
        app.setStoreProductDescription(nullIfEmpty(req.getStoreProductDescription()));
        app.setStoreCount(parseInt(req.getStoreCount()));
        app.setStoreAveragePrice(parseInt(req.getStoreAveragePrice()));
        app.setStoreBankAccount(nullIfEmpty(req.getStoreBankAccount()));
        app.setStoreReceiptName(nullIfEmpty(req.getStoreReceiptName()));
        app.setShopZipCode(nullIfEmpty(req.getShopZipCode()));
        app.setShopPrefecture(nullIfEmpty(req.getShopPrefecture()));
        app.setShopPrefectureKana(nullIfEmpty(req.getShopPrefectureKana()));
        app.setShopCity(nullIfEmpty(req.getShopCity()));
        app.setShopCityKana(nullIfEmpty(req.getShopCityKana()));
        app.setShopTown(nullIfEmpty(req.getShopTown()));
        app.setShopTownKana(nullIfEmpty(req.getShopTownKana()));
        app.setShopStreetNumber(nullIfEmpty(req.getShopStreetNumber()));
        app.setShopStreetNumberKana(nullIfEmpty(req.getShopStreetNumberKana()));
        app.setShopBuilding(nullIfEmpty(req.getShopBuilding()));
        app.setShopBuildingKana(nullIfEmpty(req.getShopBuildingKana()));
        app.setShopPhone(nullIfEmpty(req.getShopPhone()));
        app.setTerminalIcStatus(nullIfEmpty(req.getTerminalIcStatus()));
        app.setTerminalPossessionStatus(nullIfEmpty(req.getTerminalPossessionStatus()));
        app.setStoreLatitude(nullIfEmpty(req.getStoreLatitude()));
        app.setStoreLongitude(nullIfEmpty(req.getStoreLongitude()));
        app.setBusinessHours1Start(parseTime(req.getBusinessHours1Start()));
        app.setBusinessHours1End(parseTime(req.getBusinessHours1End()));
        app.setBusinessHours2Start(parseTime(req.getBusinessHours2Start()));
        app.setBusinessHours2End(parseTime(req.getBusinessHours2End()));
        app.setClosedMonday(req.isClosedMonday());
        app.setClosedTuesday(req.isClosedTuesday());
        app.setClosedWednesday(req.isClosedWednesday());
        app.setClosedThursday(req.isClosedThursday());
        app.setClosedFriday(req.isClosedFriday());
        app.setClosedSaturday(req.isClosedSaturday());
        app.setClosedSunday(req.isClosedSunday());
        app.setClosedHoliday(req.isClosedHoliday());
        app.setClosedHolidayEve(req.isClosedHolidayEve());
        app.setShopBusinessPermitNumber(nullIfEmpty(req.getShopBusinessPermitNumber()));
    }

    private void applyStep8(MerchantApplication app, MerchantApplicationRequest req) {
        app.setMposQuantity(parseInt(req.getMposQuantity()));
        app.setDeliveryZipCode(nullIfEmpty(req.getDeliveryZipCode()));
        app.setDeliveryPrefecture(nullIfEmpty(req.getDeliveryPrefecture()));
        app.setDeliveryPrefectureKana(nullIfEmpty(req.getDeliveryPrefectureKana()));
        app.setDeliveryCity(nullIfEmpty(req.getDeliveryCity()));
        app.setDeliveryCityKana(nullIfEmpty(req.getDeliveryCityKana()));
        app.setDeliveryTown(nullIfEmpty(req.getDeliveryTown()));
        app.setDeliveryTownKana(nullIfEmpty(req.getDeliveryTownKana()));
        app.setDeliveryStreetNumber(nullIfEmpty(req.getDeliveryStreetNumber()));
        app.setDeliveryStreetNumberKana(nullIfEmpty(req.getDeliveryStreetNumberKana()));
        app.setDeliveryBuilding(nullIfEmpty(req.getDeliveryBuilding()));
        app.setDeliveryBuildingKana(nullIfEmpty(req.getDeliveryBuildingKana()));
        app.setDeliveryPhone(nullIfEmpty(req.getDeliveryPhone()));
        app.setDeliveryReceiver(nullIfEmpty(req.getDeliveryReceiver()));
    }

    private void collectDocumentFiles(
            List<MerchantApplicationDocument> docs,
            String memberCode,
            List<MultipartFile> files,
            String documentType,
            LocalDateTime uploadedAt) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            collectDocumentFile(docs, memberCode, file, documentType, uploadedAt);
        }
    }

    /**
     * アップロードされたファイルの受付情報を docs リストへ追加する。
     *
     * <p>現在の実装はファイル名のみ記録する仮実装（file_path は空文字）。
     * 実際のファイル保存は未実装（保存先未定）。
     * 将来ディスク保存を追加する場合は、このメソッド内で行い、
     * ディスク書き込みの成否はトランザクション外で個別に管理すること。
     * DB 書き込みは saveDocumentsBatch() を経由し @Transactional スコープ内で完結する。
     */
    private void collectDocumentFile(
            List<MerchantApplicationDocument> docs,
            String memberCode,
            MultipartFile file,
            String documentType,
            LocalDateTime uploadedAt) {
        if (file == null || file.isEmpty()) {
            return;
        }

        String originalName = file.getOriginalFilename();
        String fileName = (originalName != null && !originalName.isEmpty())
                ? originalName : "(不明)";

        MerchantApplicationDocument doc = new MerchantApplicationDocument();
        doc.setMemberCode(memberCode);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        doc.setFilePath("");
        doc.setFileSize(file.getSize());
        doc.setUploadedAt(uploadedAt);
        docs.add(doc);
    }

    /**
     * 書類ファイルの受付情報を 1 回のバッチ INSERT で保存する。
     * GenerationType.IDENTITY では Hibernate のバッチ最適化が効かないため
     * JdbcTemplate.batchUpdate() で直接 SQL を発行する。
     */
    private void saveDocumentsBatch(List<MerchantApplicationDocument> docs) {
        if (docs.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(SQL_INSERT_DOCUMENT,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MerchantApplicationDocument doc = docs.get(i);
                        ps.setString(1, doc.getMemberCode());
                        ps.setString(2, doc.getDocumentType());
                        ps.setString(3, doc.getFileName());
                        ps.setString(4, doc.getFilePath());
                        if (doc.getFileSize() != null) {
                            ps.setLong(5, doc.getFileSize());
                        } else {
                            ps.setNull(5, Types.BIGINT);
                        }
                        ps.setObject(6, doc.getUploadedAt());
                    }

                    @Override
                    public int getBatchSize() {
                        return docs.size();
                    }
                });
        log.debug("書類ファイルを一括登録: {}件", docs.size());
    }

    private static final String MEMBER_CODE_SEQ_NAME = "member_code";
    private static final String SQL_INSERT_DOCUMENT =
            "INSERT INTO m_merchant_application_document "
            + "(member_code, document_type, file_name, file_path, file_size, uploaded_at) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * 採番管理テーブルで次の会員コードを採番する。
     * 形式は「MA-YYYY-NNNNN」（例: MA-2026-00001）。
     * 年が変わると連番をリセットする。
     */
    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(
                    SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private String generateNextMemberCode() {
        int year = Year.now().getValue();
        jdbcTemplate.update(
                "INSERT INTO m_sequence (seq_name, seq_year, last_value) "
                + "VALUES (?, ?, 0) ON CONFLICT DO NOTHING",
                MEMBER_CODE_SEQ_NAME, year);
        Long next = jdbcTemplate.queryForObject(
                "UPDATE m_sequence SET last_value = last_value + 1 "
                + "WHERE seq_name = ? AND seq_year = ? "
                + "RETURNING last_value",
                Long.class,
                MEMBER_CODE_SEQ_NAME, year);
        return String.format("MA-%d-%05d", year, next);
    }

    private String nullIfEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
