package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 申込内容照会・編集・削除を担うサービス。
 */
@Service
public class MerchantApplicationInquiryService {

    private final MerchantApplicationRepository applicationRepository;
    private final MerchantApplicationDocumentRepository documentRepository;

    public MerchantApplicationInquiryService(
            MerchantApplicationRepository applicationRepository,
            MerchantApplicationDocumentRepository documentRepository) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public MerchantApplication findApplication(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            return null;
        }
        return applicationRepository.findByMemberCodeAndDeleteFlagFalse(memberCode).orElse(null);
    }

    /**
     * 書類情報を documentType をキーとした Map で返す。
     */
    @Transactional(readOnly = true)
    public Map<String, MerchantApplicationDocument> findDocumentMap(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            return Map.of();
        }
        return documentRepository.findByMemberCodeAndDeleteFlagFalse(memberCode).stream()
                .collect(Collectors.toMap(
                        MerchantApplicationDocument::getDocumentType,
                        d -> d,
                        (a, b) -> a
                ));
    }

    /**
     * 申込情報を更新する。
     * クライアントから受け取った文字列 Map をパースして各フィールドに反映し保存する。
     */
    @Transactional
    public void updateApplication(Map<String, String> data) {
        String memberCode = data.get("memberCode");
        if (memberCode == null || memberCode.isBlank()) {
            throw new IllegalArgumentException("会員コードが指定されていません");
        }
        MerchantApplication e = applicationRepository.findByMemberCodeAndDeleteFlagFalse(memberCode)
                .orElseThrow(() -> new IllegalArgumentException("申込情報が見つかりません: " + memberCode));

        validateUpdateData(data);

        e.setApplicationStatus(s(data, "applicationStatus"));

        // STEP 1
        e.setAgreedStarpay(b(data, "agreedStarpay"));
        e.setAgreedJcb(b(data, "agreedJcb"));
        e.setAgreedRyuginVisaMcCu(b(data, "agreedRyuginVisaMcCu"));
        e.setAgreedRyuginCuQr(b(data, "agreedRyuginCuQr"));
        e.setAgreedAgencyDelegation(b(data, "agreedAgencyDelegation"));
        e.setAgreedServiceTerms(b(data, "agreedServiceTerms"));
        e.setAgreedPrivacyPolicy(b(data, "agreedPrivacyPolicy"));
        e.setAgreedAuthorityConfirmed(b(data, "agreedAuthorityConfirmed"));

        // STEP 2
        e.setTxTypeVisitSales(b(data, "txTypeVisitSales"));
        e.setTxTypeContinuousService(b(data, "txTypeContinuousService"));
        e.setTxTypePhoneSolicitation(b(data, "txTypePhoneSolicitation"));
        e.setTxTypePrepaidService(b(data, "txTypePrepaidService"));
        e.setTxTypeBusinessInduction(b(data, "txTypeBusinessInduction"));
        e.setTxTypeChainSales(b(data, "txTypeChainSales"));
        e.setTxTypeNoneApplicable(b(data, "txTypeNoneApplicable"));
        e.setBusinessEntityType(s(data, "businessEntityType"));
        e.setSalesFormat(s(data, "salesFormat"));
        e.setOperationFormat(s(data, "operationFormat"));

        // STEP 3 QR
        e.setPayQrPaypay(b(data, "payQrPaypay"));
        e.setPayQrDBarai(b(data, "payQrDBarai"));
        e.setPayQrRakutenPay(b(data, "payQrRakutenPay"));
        e.setPayQrAlipayPlus(b(data, "payQrAlipayPlus"));
        e.setPayQrWechatPay(b(data, "payQrWechatPay"));
        e.setPayQrAuPay(b(data, "payQrAuPay"));
        e.setPayQrMerpay(b(data, "payQrMerpay"));
        e.setPayQrJkoPay(b(data, "payQrJkoPay"));

        // STEP 3 クレジット
        e.setPayCreditJcb(b(data, "payCreditJcb"));
        e.setPayCreditVisa(b(data, "payCreditVisa"));
        e.setPayCreditMastercard(b(data, "payCreditMastercard"));
        e.setPayCreditDiscover(b(data, "payCreditDiscover"));
        e.setPayCreditDiners(b(data, "payCreditDiners"));
        e.setPayCreditAmex(b(data, "payCreditAmex"));
        e.setPayCreditBonus(s(data, "payCreditBonus"));
        e.setPayCreditTwoTimes(s(data, "payCreditTwoTimes"));
        e.setPayCreditInstallment(s(data, "payCreditInstallment"));
        e.setPayCreditRevolving(s(data, "payCreditRevolving"));

        // STEP 3 電子マネー
        e.setPayEmoneyId(b(data, "payEmoneyId"));
        e.setPayEmoneyWaon(b(data, "payEmoneyWaon"));
        e.setPayEmoneyRakutenEdy(b(data, "payEmoneyRakutenEdy"));
        e.setPayEmoneyNanaco(b(data, "payEmoneyNanaco"));
        e.setPayEmoneyTransitIc(b(data, "payEmoneyTransitIc"));
        e.setPayEmoneyQuickPay(b(data, "payEmoneyQuickPay"));
        e.setPayEmoneyApplePay(b(data, "payEmoneyApplePay"));

        // STEP 4 法人情報
        e.setCorporateNumber(s(data, "corporateNumber"));
        e.setCorporateName(s(data, "corporateName"));
        e.setCorporateNameKana(s(data, "corporateNameKana"));
        e.setCorporateNameEn(s(data, "corporateNameEn"));
        e.setEstablishmentDate(d(data, "establishmentDate"));
        e.setCorporateType(s(data, "corporateType"));
        e.setBrandName(s(data, "brandName"));
        e.setBrandNameKana(s(data, "brandNameKana"));
        e.setBrandNameEn(s(data, "brandNameEn"));
        e.setCompanyUrl(s(data, "companyUrl"));
        e.setAnnualRevenue(l(data, "annualRevenue"));
        e.setCapitalAmount(l(data, "capitalAmount"));
        e.setEmployeeCount(i(data, "employeeCount"));
        e.setIndustryCategory(s(data, "industryCategory"));
        e.setIndustryDetail(s(data, "industryDetail"));
        e.setBusinessDescription(s(data, "businessDescription"));

        // 本社住所
        e.setCompanyZipCode(s(data, "companyZipCode"));
        e.setCompanyPrefecture(s(data, "companyPrefecture"));
        e.setCompanyPrefectureKana(s(data, "companyPrefectureKana"));
        e.setCompanyCity(s(data, "companyCity"));
        e.setCompanyCityKana(s(data, "companyCityKana"));
        e.setCompanyTown(s(data, "companyTown"));
        e.setCompanyTownKana(s(data, "companyTownKana"));
        e.setCompanyStreetNumber(s(data, "companyStreetNumber"));
        e.setCompanyStreetNumberKana(s(data, "companyStreetNumberKana"));
        e.setCompanyBuilding(s(data, "companyBuilding"));
        e.setCompanyBuildingKana(s(data, "companyBuildingKana"));
        e.setCompanyPhone(s(data, "companyPhone"));
        e.setCompanyFax(s(data, "companyFax"));
        e.setCompanyMobile(s(data, "companyMobile"));

        // 代表者
        e.setRepLastName(s(data, "repLastName"));
        e.setRepLastNameKana(s(data, "repLastNameKana"));
        e.setRepLastNameEn(s(data, "repLastNameEn"));
        e.setRepFirstName(s(data, "repFirstName"));
        e.setRepFirstNameKana(s(data, "repFirstNameKana"));
        e.setRepFirstNameEn(s(data, "repFirstNameEn"));
        e.setRepBirthDate(d(data, "repBirthDate"));
        e.setRepGender(s(data, "repGender"));
        e.setRepZipCode(s(data, "repZipCode"));
        e.setRepPrefecture(s(data, "repPrefecture"));
        e.setRepPrefectureKana(s(data, "repPrefectureKana"));
        e.setRepCity(s(data, "repCity"));
        e.setRepCityKana(s(data, "repCityKana"));
        e.setRepTown(s(data, "repTown"));
        e.setRepTownKana(s(data, "repTownKana"));
        e.setRepStreetNumber(s(data, "repStreetNumber"));
        e.setRepStreetNumberKana(s(data, "repStreetNumberKana"));
        e.setRepBuilding(s(data, "repBuilding"));
        e.setRepPhone(s(data, "repPhone"));

        // 担当者
        e.setContactLastName(s(data, "contactLastName"));
        e.setContactLastNameKana(s(data, "contactLastNameKana"));
        e.setContactFirstName(s(data, "contactFirstName"));
        e.setContactFirstNameKana(s(data, "contactFirstNameKana"));
        e.setContactZipCode(s(data, "contactZipCode"));
        e.setContactPrefecture(s(data, "contactPrefecture"));
        e.setContactPrefectureKana(s(data, "contactPrefectureKana"));
        e.setContactCity(s(data, "contactCity"));
        e.setContactCityKana(s(data, "contactCityKana"));
        e.setContactTown(s(data, "contactTown"));
        e.setContactTownKana(s(data, "contactTownKana"));
        e.setContactStreetNumber(s(data, "contactStreetNumber"));
        e.setContactStreetNumberKana(s(data, "contactStreetNumberKana"));
        e.setContactBuilding(s(data, "contactBuilding"));
        e.setContactBuildingKana(s(data, "contactBuildingKana"));
        e.setContactDepartment(s(data, "contactDepartment"));
        e.setContactEmail(s(data, "contactEmail"));
        e.setContactPhone1(s(data, "contactPhone1"));
        e.setContactPhone2(s(data, "contactPhone2"));

        // STEP 5 口座
        e.setBankCode(s(data, "bankCode"));
        e.setBankName(s(data, "bankName"));
        e.setBranchCode(s(data, "branchCode"));
        e.setBranchName(s(data, "branchName"));
        e.setAccountType(s(data, "accountType"));
        e.setAccountNumber(s(data, "accountNumber"));
        e.setAccountHolderKana(s(data, "accountHolderKana"));

        // STEP 6 店舗
        e.setStoreName(s(data, "storeName"));
        e.setStoreNameKana(s(data, "storeNameKana"));
        e.setStoreNameEn(s(data, "storeNameEn"));
        e.setStoreBrandName(s(data, "storeBrandName"));
        e.setStoreBrandNameKana(s(data, "storeBrandNameKana"));
        e.setStoreBrandNameEn(s(data, "storeBrandNameEn"));
        e.setStoreIndustryCategory(s(data, "storeIndustryCategory"));
        e.setStoreIndustryDetail(s(data, "storeIndustryDetail"));
        e.setStoreProductDescription(s(data, "storeProductDescription"));
        e.setStoreCount(i(data, "storeCount"));
        e.setStoreAveragePrice(i(data, "storeAveragePrice"));
        e.setStoreBankAccount(s(data, "storeBankAccount"));
        e.setStoreReceiptName(s(data, "storeReceiptName"));
        e.setMapDisplayDesired(b(data, "mapDisplayDesired"));
        e.setMapDisplayDesiredDate(d(data, "mapDisplayDesiredDate"));
        e.setStoreLatitude(s(data, "storeLatitude"));
        e.setStoreLongitude(s(data, "storeLongitude"));
        e.setBusinessHours1Start(t(data, "businessHours1Start"));
        e.setBusinessHours1End(t(data, "businessHours1End"));
        e.setBusinessHours2Start(t(data, "businessHours2Start"));
        e.setBusinessHours2End(t(data, "businessHours2End"));
        e.setClosedMonday(b(data, "closedMonday"));
        e.setClosedTuesday(b(data, "closedTuesday"));
        e.setClosedWednesday(b(data, "closedWednesday"));
        e.setClosedThursday(b(data, "closedThursday"));
        e.setClosedFriday(b(data, "closedFriday"));
        e.setClosedSaturday(b(data, "closedSaturday"));
        e.setClosedSunday(b(data, "closedSunday"));
        e.setClosedHoliday(b(data, "closedHoliday"));
        e.setClosedHolidayEve(b(data, "closedHolidayEve"));

        // 店舗住所
        e.setShopZipCode(s(data, "shopZipCode"));
        e.setShopPrefecture(s(data, "shopPrefecture"));
        e.setShopPrefectureKana(s(data, "shopPrefectureKana"));
        e.setShopCity(s(data, "shopCity"));
        e.setShopCityKana(s(data, "shopCityKana"));
        e.setShopTown(s(data, "shopTown"));
        e.setShopTownKana(s(data, "shopTownKana"));
        e.setShopStreetNumber(s(data, "shopStreetNumber"));
        e.setShopStreetNumberKana(s(data, "shopStreetNumberKana"));
        e.setShopBuilding(s(data, "shopBuilding"));
        e.setShopBuildingKana(s(data, "shopBuildingKana"));
        e.setShopPhone(s(data, "shopPhone"));
        e.setShopBusinessPermitNumber(s(data, "shopBusinessPermitNumber"));
        e.setTerminalPossessionStatus(s(data, "terminalPossessionStatus"));
        e.setTerminalIcStatus(s(data, "terminalIcStatus"));

        // STEP 8 発送
        e.setMposQuantity(i(data, "mposQuantity"));
        e.setDeliveryZipCode(s(data, "deliveryZipCode"));
        e.setDeliveryPrefecture(s(data, "deliveryPrefecture"));
        e.setDeliveryPrefectureKana(s(data, "deliveryPrefectureKana"));
        e.setDeliveryCity(s(data, "deliveryCity"));
        e.setDeliveryCityKana(s(data, "deliveryCityKana"));
        e.setDeliveryTown(s(data, "deliveryTown"));
        e.setDeliveryTownKana(s(data, "deliveryTownKana"));
        e.setDeliveryStreetNumber(s(data, "deliveryStreetNumber"));
        e.setDeliveryStreetNumberKana(s(data, "deliveryStreetNumberKana"));
        e.setDeliveryBuilding(s(data, "deliveryBuilding"));
        e.setDeliveryBuildingKana(s(data, "deliveryBuildingKana"));
        e.setDeliveryPhone(s(data, "deliveryPhone"));
        e.setDeliveryReceiver(s(data, "deliveryReceiver"));

        applicationRepository.save(e);
    }

    private static void validateUpdateData(Map<String, String> data) {
        List<String> errors = new ArrayList<>();

        // STEP 1: 事前確認（全同意が必須）
        if (!"true".equals(data.get("agreedStarpay"))) {
            errors.add("StarPay決済サービス加盟店規約への確認が必要です");
        }
        if (!"true".equals(data.get("agreedJcb"))) {
            errors.add("JCB加盟店規約・加盟店特約への確認が必要です");
        }
        if (!"true".equals(data.get("agreedRyuginVisaMcCu"))) {
            errors.add("琉球銀行加盟店規約（Visa/Mastercard/銀聯）への確認が必要です");
        }
        if (!"true".equals(data.get("agreedRyuginCuQr"))) {
            errors.add("銀聯QRコード決済サービス利用加盟店規約への確認が必要です");
        }
        if (!"true".equals(data.get("agreedAgencyDelegation"))) {
            errors.add("代理申請の委任への同意が必要です");
        }
        if (!"true".equals(data.get("agreedServiceTerms"))) {
            errors.add("当社サービス利用規約への同意が必要です");
        }
        if (!"true".equals(data.get("agreedPrivacyPolicy"))) {
            errors.add("当社プライバシーポリシーへの同意が必要です");
        }
        if (!"true".equals(data.get("agreedAuthorityConfirmed"))) {
            errors.add("代表者・契約締結権限の確認が必要です");
        }

        // STEP 2: 取引形態
        boolean txAny = "true".equals(data.get("txTypeVisitSales"))
                || "true".equals(data.get("txTypeContinuousService"))
                || "true".equals(data.get("txTypePhoneSolicitation"))
                || "true".equals(data.get("txTypePrepaidService"))
                || "true".equals(data.get("txTypeBusinessInduction"))
                || "true".equals(data.get("txTypeChainSales"))
                || "true".equals(data.get("txTypeNoneApplicable"));
        if (!txAny) {
            errors.add("取引形態を1つ以上選択してください");
        }
        requireField(data, "businessEntityType", "法人区分", errors);
        requireField(data, "salesFormat", "販売形態", errors);
        requireField(data, "operationFormat", "運営形態", errors);

        // STEP 3: 決済種類
        boolean qrAny = "true".equals(data.get("payQrWechatPay"))
                || "true".equals(data.get("payQrPaypay"))
                || "true".equals(data.get("payQrDBarai"))
                || "true".equals(data.get("payQrAuPay"))
                || "true".equals(data.get("payQrMerpay"))
                || "true".equals(data.get("payQrRakutenPay"))
                || "true".equals(data.get("payQrAlipayPlus"))
                || "true".equals(data.get("payQrJkoPay"));
        if (!qrAny) {
            errors.add("QRコード決済を1つ以上選択してください");
        }

        boolean creditAny = "true".equals(data.get("payCreditJcb"))
                || "true".equals(data.get("payCreditVisa"))
                || "true".equals(data.get("payCreditMastercard"))
                || "true".equals(data.get("payCreditDiscover"))
                || "true".equals(data.get("payCreditDiners"))
                || "true".equals(data.get("payCreditAmex"));
        if (!creditAny) {
            errors.add("クレジットカード決済を1つ以上選択してください");
        }

        boolean emoneyAny = "true".equals(data.get("payEmoneyId"))
                || "true".equals(data.get("payEmoneyWaon"))
                || "true".equals(data.get("payEmoneyRakutenEdy"))
                || "true".equals(data.get("payEmoneyNanaco"))
                || "true".equals(data.get("payEmoneyTransitIc"))
                || "true".equals(data.get("payEmoneyQuickPay"))
                || "true".equals(data.get("payEmoneyApplePay"));
        if (!emoneyAny) {
            errors.add("電子マネー決済を1つ以上選択してください");
        }

        // STEP 4: 法人情報
        requireField(data, "corporateNumber", "法人番号", errors);
        requireField(data, "corporateName", "法人名", errors);
        requireField(data, "corporateNameKana", "法人名（カナ）", errors);
        requireField(data, "corporateNameEn", "法人名（英語）", errors);
        requireField(data, "establishmentDate", "法人設立年月日", errors);
        requireField(data, "brandName", "ブランド名（屋号）", errors);
        requireField(data, "brandNameKana", "ブランド名（カナ）", errors);
        requireField(data, "brandNameEn", "ブランド名（英語）", errors);
        requireField(data, "annualRevenue", "年商", errors);
        requireField(data, "industryCategory", "業種（カテゴリー）", errors);
        requireField(data, "industryDetail", "業種（詳細）", errors);
        requireField(data, "businessDescription", "事業内容及び取扱商材", errors);

        // 代表者
        requireField(data, "repLastName", "代表者 姓", errors);
        requireField(data, "repLastNameKana", "代表者 姓（カナ）", errors);
        requireField(data, "repLastNameEn", "代表者 姓（英語）", errors);
        requireField(data, "repFirstName", "代表者 名", errors);
        requireField(data, "repFirstNameKana", "代表者 名（カナ）", errors);
        requireField(data, "repFirstNameEn", "代表者 名（英語）", errors);
        requireField(data, "repBirthDate", "代表者 生年月日", errors);
        requireField(data, "repGender", "代表者 性別", errors);
        requireField(data, "repZipCode", "代表者 郵便番号", errors);
        requireField(data, "repPrefecture", "代表者 都道府県", errors);
        requireField(data, "repCity", "代表者 市区町村", errors);
        requireField(data, "repTown", "代表者 町域", errors);
        requireField(data, "repStreetNumber", "代表者 番地", errors);
        requireField(data, "repPhone", "代表者 電話番号", errors);

        // 担当者
        requireField(data, "contactLastName", "担当者 姓", errors);
        requireField(data, "contactLastNameKana", "担当者 姓（カナ）", errors);
        requireField(data, "contactFirstName", "担当者 名", errors);
        requireField(data, "contactFirstNameKana", "担当者 名（カナ）", errors);
        requireField(data, "contactDepartment", "担当者 部署名", errors);
        requireField(data, "contactEmail", "担当者 メールアドレス", errors);
        requireField(data, "contactPhone1", "担当者 電話番号1", errors);
        requireField(data, "contactZipCode", "担当者 郵便番号", errors);
        requireField(data, "contactPrefecture", "担当者 都道府県", errors);
        requireField(data, "contactCity", "担当者 市区町村", errors);
        requireField(data, "contactTown", "担当者 町域", errors);
        requireField(data, "contactStreetNumber", "担当者 番地", errors);

        // STEP 5: 口座情報
        requireField(data, "bankCode", "金融機関コード", errors);
        requireField(data, "bankName", "金融機関名", errors);
        requireField(data, "branchCode", "支店コード", errors);
        requireField(data, "branchName", "支店名", errors);
        requireField(data, "accountType", "預金種別", errors);
        requireField(data, "accountNumber", "口座番号", errors);
        requireField(data, "accountHolderKana", "口座名義（カナ）", errors);

        // STEP 6: 店舗情報
        requireField(data, "storeName", "店舗名", errors);
        requireField(data, "storeNameKana", "店舗名（カナ）", errors);
        requireField(data, "storeNameEn", "店舗名（英語）", errors);
        requireField(data, "storeBrandName", "店舗ブランド名", errors);
        requireField(data, "storeBrandNameKana", "店舗ブランド名（カナ）", errors);
        requireField(data, "storeBrandNameEn", "店舗ブランド名（英語）", errors);
        requireField(data, "storeIndustryCategory", "店舗業種（カテゴリ）", errors);
        requireField(data, "storeIndustryDetail", "店舗業種（詳細）", errors);
        requireField(data, "storeProductDescription", "店舗商材の詳細", errors);

        String storeCountStr = data.get("storeCount");
        if (storeCountStr == null || storeCountStr.isBlank()) {
            errors.add("店舗数は必須です");
        } else {
            try {
                int cnt = Integer.parseInt(storeCountStr.trim());
                if (cnt < 1) {
                    errors.add("店舗数は1以上を入力してください");
                }
            } catch (NumberFormatException ignore) {
                errors.add("店舗数に正しい数値を入力してください");
            }
        }

        String avgPriceStr = data.get("storeAveragePrice");
        if (avgPriceStr == null || avgPriceStr.isBlank()) {
            errors.add("平均単価は必須です");
        } else {
            try {
                int price = Integer.parseInt(avgPriceStr.trim());
                if (price < 0) {
                    errors.add("平均単価は0以上を入力してください");
                }
            } catch (NumberFormatException ignore) {
                errors.add("平均単価に正しい数値を入力してください");
            }
        }

        requireField(data, "storeBankAccount", "店舗口座", errors);
        requireField(data, "storeReceiptName", "レシート名", errors);
        requireField(data, "shopZipCode", "店舗 郵便番号", errors);
        requireField(data, "shopPrefecture", "店舗 都道府県", errors);
        requireField(data, "shopCity", "店舗 市区町村", errors);
        requireField(data, "shopTown", "店舗 町域", errors);
        requireField(data, "shopStreetNumber", "店舗 番地", errors);
        requireField(data, "shopPhone", "店舗 電話番号", errors);
        requireField(data, "terminalPossessionStatus", "端末保持状況", errors);

        // STEP 8: 発送情報
        String mposQuantityStr = data.get("mposQuantity");
        if (mposQuantityStr == null || mposQuantityStr.isBlank()) {
            errors.add("端末台数は必須です");
        } else {
            try {
                int qty = Integer.parseInt(mposQuantityStr.trim());
                if (qty < 1) {
                    errors.add("端末台数は1以上を入力してください");
                }
            } catch (NumberFormatException ignore) {
                errors.add("端末台数に正しい数値を入力してください");
            }
        }
        requireField(data, "deliveryZipCode", "お届け先 郵便番号", errors);
        requireField(data, "deliveryPrefecture", "お届け先 都道府県", errors);
        requireField(data, "deliveryCity", "お届け先 市区町村", errors);
        requireField(data, "deliveryTown", "お届け先 町域", errors);
        requireField(data, "deliveryStreetNumber", "お届け先 番地", errors);
        requireField(data, "deliveryPhone", "お届け先 電話番号", errors);
        requireField(data, "deliveryReceiver", "受取人", errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    private static void requireField(
            Map<String, String> data, String key, String label, List<String> errors) {
        String v = data.get(key);
        if (v == null || v.isBlank()) {
            errors.add(label + "は必須です");
        }
    }

    /**
     * 申込情報を論理削除する（delete_flag を true に設定）。
     */
    @Transactional
    public void deleteApplication(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            throw new IllegalArgumentException("会員コードが指定されていません");
        }
        MerchantApplication e = applicationRepository.findByMemberCodeAndDeleteFlagFalse(memberCode)
                .orElseThrow(() -> new IllegalArgumentException("申込情報が見つかりません: " + memberCode));
        e.setDeleteFlag(true);
        documentRepository.logicalDeleteByMemberCode(memberCode);
    }

    private static String s(Map<String, String> d, String k) {
        String v = d.get(k);
        return (v == null || v.isBlank()) ? null : v;
    }

    private static boolean b(Map<String, String> d, String k) {
        return "true".equals(d.get(k));
    }

    private static LocalDate d(Map<String, String> d, String k) {
        String v = d.get(k);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日付の形式が正しくありません: " + k, ex);
        }
    }

    private static LocalTime t(Map<String, String> d, String k) {
        String v = d.get(k);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("時刻の形式が正しくありません: " + k, ex);
        }
    }

    private static Integer i(Map<String, String> d, String k) {
        String v = d.get(k);
        return (v == null || v.isBlank()) ? null : Integer.parseInt(v);
    }

    private static Long l(Map<String, String> d, String k) {
        String v = d.get(k);
        return (v == null || v.isBlank()) ? null : Long.parseLong(v);
    }
}
