package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MerchantApplicationRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantApplicationResponse;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
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

        String memberCode = generateNextMemberCode();
        String tempPassword = generateTempPassword();
        LocalDateTime now = LocalDateTime.now();

        MerchantApplication application = buildApplication(request, memberCode, now);
        application.setTempPasswordHash(passwordEncoder.encode(tempPassword));
        merchantApplicationRepository.save(application);

        saveDocumentFiles(memberCode, businessPermits,
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, now);
        saveDocumentFile(memberCode, idDocFront,
                MerchantApplicationDocument.TYPE_ID_FRONT, now);
        saveDocumentFile(memberCode, idDocBack,
                MerchantApplicationDocument.TYPE_ID_BACK, now);
        saveDocumentFiles(memberCode, openingPlans,
                MerchantApplicationDocument.TYPE_OPENING_PLAN, now);
        saveDocumentFiles(memberCode, productMaterials,
                MerchantApplicationDocument.TYPE_PRODUCT_MATERIAL, now);
        saveDocumentFiles(memberCode, eventVenues,
                MerchantApplicationDocument.TYPE_EVENT_VENUE, now);

        log.info("加盟店申込を登録しました: memberCode={}", memberCode);
        return new MerchantApplicationResponse(true, memberCode, tempPassword, null);
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

        // STEP 1
        app.setAgreedStarpay(req.isAgreedStarpay());
        app.setAgreedJcb(req.isAgreedJcb());
        app.setAgreedRyuginVisaMcCu(req.isAgreedRyuginVisaMcCu());
        app.setAgreedRyuginCuQr(req.isAgreedRyuginCuQr());
        app.setAgreedAgencyDelegation(req.isAgreedAgencyDelegation());
        app.setAgreedServiceTerms(req.isAgreedServiceTerms());
        app.setAgreedPrivacyPolicy(req.isAgreedPrivacyPolicy());
        app.setAgreedAuthorityConfirmed(req.isAgreedAuthorityConfirmed());

        // STEP 2
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

        // STEP 3
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

        // STEP 4
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

        // STEP 5
        app.setBankCode(nullIfEmpty(req.getBankCode()));
        app.setBankName(nullIfEmpty(req.getBankName()));
        app.setBranchCode(nullIfEmpty(req.getBranchCode()));
        app.setBranchName(nullIfEmpty(req.getBranchName()));
        app.setAccountType(nullIfEmpty(req.getAccountType()));
        app.setAccountNumber(nullIfEmpty(req.getAccountNumber()));
        app.setAccountHolderKana(nullIfEmpty(req.getAccountHolderKana()));

        // STEP 6
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
        app.setClosedSunday(req.isClosedSunday());
        app.setClosedHoliday(req.isClosedHoliday());
        app.setClosedHolidayEve(req.isClosedHolidayEve());
        app.setShopBusinessPermitNumber(nullIfEmpty(req.getShopBusinessPermitNumber()));

        // STEP 8
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

        return app;
    }

    private void saveDocumentFiles(
            String memberCode,
            List<MultipartFile> files,
            String documentType,
            LocalDateTime uploadedAt) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            saveDocumentFile(memberCode, file, documentType, uploadedAt);
        }
    }

    private void saveDocumentFile(
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
        documentRepository.save(doc);
    }

    private static final String MEMBER_CODE_SEQ_NAME = "member_code";

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
