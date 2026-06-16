package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
        return applicationRepository.findById(memberCode).orElse(null);
    }

    /**
     * 書類情報を documentType をキーとした Map で返す。
     */
    @Transactional(readOnly = true)
    public Map<String, MerchantApplicationDocument> findDocumentMap(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            return Map.of();
        }
        return documentRepository.findByMemberCode(memberCode).stream()
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
        MerchantApplication e = applicationRepository.findById(memberCode)
                .orElseThrow(() -> new IllegalArgumentException("申込情報が見つかりません: " + memberCode));

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

    /**
     * 申込情報および添付書類を削除する。
     */
    @Transactional
    public void deleteApplication(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            throw new IllegalArgumentException("会員コードが指定されていません");
        }
        documentRepository.deleteByMemberCode(memberCode);
        applicationRepository.deleteById(memberCode);
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
        return (v == null || v.isBlank()) ? null : LocalDate.parse(v);
    }

    private static LocalTime t(Map<String, String> d, String k) {
        String v = d.get(k);
        return (v == null || v.isBlank()) ? null : LocalTime.parse(v);
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
