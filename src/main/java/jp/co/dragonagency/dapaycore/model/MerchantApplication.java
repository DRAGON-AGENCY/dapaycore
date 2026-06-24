package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 加盟店申込を表すエンティティ。
 * m_merchant_application テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_merchant_application")
public class MerchantApplication {

    public static final String STATUS_UNREVIEWED = "UNREVIEWED";
    public static final String STATUS_REVIEWING = "REVIEWING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @Column(name = "member_code")
    private String memberCode;

    @Column(name = "application_status")
    private String applicationStatus;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "update_user_id")
    private String updateUserId;

    // STEP 1
    @Column(name = "agreed_starpay")
    private boolean agreedStarpay;

    @Column(name = "agreed_jcb")
    private boolean agreedJcb;

    @Column(name = "agreed_ryugin_visa_mc_cu")
    private boolean agreedRyuginVisaMcCu;

    @Column(name = "agreed_ryugin_cu_qr")
    private boolean agreedRyuginCuQr;

    @Column(name = "agreed_agency_delegation")
    private boolean agreedAgencyDelegation;

    @Column(name = "agreed_service_terms")
    private boolean agreedServiceTerms;

    @Column(name = "agreed_privacy_policy")
    private boolean agreedPrivacyPolicy;

    @Column(name = "agreed_authority_confirmed")
    private boolean agreedAuthorityConfirmed;

    // STEP 2
    @Column(name = "tx_type_visit_sales")
    private boolean txTypeVisitSales;

    @Column(name = "tx_type_continuous_service")
    private boolean txTypeContinuousService;

    @Column(name = "tx_type_phone_solicitation")
    private boolean txTypePhoneSolicitation;

    @Column(name = "tx_type_prepaid_service")
    private boolean txTypePrepaidService;

    @Column(name = "tx_type_business_induction")
    private boolean txTypeBusinessInduction;

    @Column(name = "tx_type_chain_sales")
    private boolean txTypeChainSales;

    @Column(name = "tx_type_none_applicable")
    private boolean txTypeNoneApplicable;

    @Column(name = "business_entity_type")
    private String businessEntityType;

    @Column(name = "sales_format")
    private String salesFormat;

    @Column(name = "operation_format")
    private String operationFormat;

    // STEP 3: QR
    @Column(name = "pay_qr_wechat_pay")
    private boolean payQrWechatPay;

    @Column(name = "pay_qr_paypay")
    private boolean payQrPaypay;

    @Column(name = "pay_qr_d_barai")
    private boolean payQrDBarai;

    @Column(name = "pay_qr_au_pay")
    private boolean payQrAuPay;

    @Column(name = "pay_qr_merpay")
    private boolean payQrMerpay;

    @Column(name = "pay_qr_rakuten_pay")
    private boolean payQrRakutenPay;

    @Column(name = "pay_qr_alipay_plus")
    private boolean payQrAlipayPlus;

    @Column(name = "pay_qr_jkopay")
    private boolean payQrJkoPay;

    // STEP 3: クレジット
    @Column(name = "pay_credit_jcb")
    private boolean payCreditJcb;

    @Column(name = "pay_credit_discover")
    private boolean payCreditDiscover;

    @Column(name = "pay_credit_visa")
    private boolean payCreditVisa;

    @Column(name = "pay_credit_mastercard")
    private boolean payCreditMastercard;

    @Column(name = "pay_credit_diners")
    private boolean payCreditDiners;

    @Column(name = "pay_credit_amex")
    private boolean payCreditAmex;

    @Column(name = "pay_credit_bonus")
    private String payCreditBonus;

    @Column(name = "pay_credit_two_times")
    private String payCreditTwoTimes;

    @Column(name = "pay_credit_installment")
    private String payCreditInstallment;

    @Column(name = "pay_credit_revolving")
    private String payCreditRevolving;

    // STEP 3: 電子マネー
    @Column(name = "pay_emoney_id")
    private boolean payEmoneyId;

    @Column(name = "pay_emoney_waon")
    private boolean payEmoneyWaon;

    @Column(name = "pay_emoney_rakuten_edy")
    private boolean payEmoneyRakutenEdy;

    @Column(name = "pay_emoney_nanaco")
    private boolean payEmoneyNanaco;

    @Column(name = "pay_emoney_transit_ic")
    private boolean payEmoneyTransitIc;

    @Column(name = "pay_emoney_quick_pay")
    private boolean payEmoneyQuickPay;

    @Column(name = "pay_emoney_apple_pay")
    private boolean payEmoneyApplePay;

    // STEP 4: 法人情報
    @Column(name = "corporate_number")
    private String corporateNumber;

    @Column(name = "corporate_name")
    private String corporateName;

    @Column(name = "corporate_name_kana")
    private String corporateNameKana;

    @Column(name = "corporate_name_en")
    private String corporateNameEn;

    @Column(name = "establishment_date")
    private LocalDate establishmentDate;

    @Column(name = "corporate_type")
    private String corporateType;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "brand_name_kana")
    private String brandNameKana;

    @Column(name = "brand_name_en")
    private String brandNameEn;

    @Column(name = "company_url")
    private String companyUrl;

    @Column(name = "annual_revenue")
    private Long annualRevenue;

    @Column(name = "capital_amount")
    private Long capitalAmount;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "industry_category")
    private String industryCategory;

    @Column(name = "industry_detail")
    private String industryDetail;

    @Column(name = "business_description")
    private String businessDescription;

    // 本社住所
    @Column(name = "company_zip_code")
    private String companyZipCode;

    @Column(name = "company_prefecture")
    private String companyPrefecture;

    @Column(name = "company_prefecture_kana")
    private String companyPrefectureKana;

    @Column(name = "company_city")
    private String companyCity;

    @Column(name = "company_city_kana")
    private String companyCityKana;

    @Column(name = "company_town")
    private String companyTown;

    @Column(name = "company_town_kana")
    private String companyTownKana;

    @Column(name = "company_street_number")
    private String companyStreetNumber;

    @Column(name = "company_street_number_kana")
    private String companyStreetNumberKana;

    @Column(name = "company_building")
    private String companyBuilding;

    @Column(name = "company_building_kana")
    private String companyBuildingKana;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_fax")
    private String companyFax;

    @Column(name = "company_mobile")
    private String companyMobile;

    // 代表者情報
    @Column(name = "rep_last_name")
    private String repLastName;

    @Column(name = "rep_last_name_kana")
    private String repLastNameKana;

    @Column(name = "rep_last_name_en")
    private String repLastNameEn;

    @Column(name = "rep_first_name")
    private String repFirstName;

    @Column(name = "rep_first_name_kana")
    private String repFirstNameKana;

    @Column(name = "rep_first_name_en")
    private String repFirstNameEn;

    @Column(name = "rep_birth_date")
    private LocalDate repBirthDate;

    @Column(name = "rep_gender")
    private String repGender;

    // 代表者自宅住所
    @Column(name = "rep_zip_code")
    private String repZipCode;

    @Column(name = "rep_prefecture")
    private String repPrefecture;

    @Column(name = "rep_prefecture_kana")
    private String repPrefectureKana;

    @Column(name = "rep_city")
    private String repCity;

    @Column(name = "rep_city_kana")
    private String repCityKana;

    @Column(name = "rep_town")
    private String repTown;

    @Column(name = "rep_town_kana")
    private String repTownKana;

    @Column(name = "rep_street_number")
    private String repStreetNumber;

    @Column(name = "rep_street_number_kana")
    private String repStreetNumberKana;

    @Column(name = "rep_building")
    private String repBuilding;

    @Column(name = "rep_phone")
    private String repPhone;

    // 担当者情報
    @Column(name = "contact_last_name")
    private String contactLastName;

    @Column(name = "contact_last_name_kana")
    private String contactLastNameKana;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_first_name_kana")
    private String contactFirstNameKana;

    // 担当者勤務先住所
    @Column(name = "contact_zip_code")
    private String contactZipCode;

    @Column(name = "contact_prefecture")
    private String contactPrefecture;

    @Column(name = "contact_prefecture_kana")
    private String contactPrefectureKana;

    @Column(name = "contact_city")
    private String contactCity;

    @Column(name = "contact_city_kana")
    private String contactCityKana;

    @Column(name = "contact_town")
    private String contactTown;

    @Column(name = "contact_town_kana")
    private String contactTownKana;

    @Column(name = "contact_street_number")
    private String contactStreetNumber;

    @Column(name = "contact_street_number_kana")
    private String contactStreetNumberKana;

    @Column(name = "contact_building")
    private String contactBuilding;

    @Column(name = "contact_building_kana")
    private String contactBuildingKana;

    @Column(name = "contact_department")
    private String contactDepartment;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone1")
    private String contactPhone1;

    @Column(name = "contact_phone2")
    private String contactPhone2;

    // STEP 5: 口座情報
    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_holder_kana")
    private String accountHolderKana;

    // STEP 6: 店舗情報
    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_name_kana")
    private String storeNameKana;

    @Column(name = "store_name_en")
    private String storeNameEn;

    @Column(name = "store_brand_name")
    private String storeBrandName;

    @Column(name = "store_brand_name_kana")
    private String storeBrandNameKana;

    @Column(name = "store_brand_name_en")
    private String storeBrandNameEn;

    @Column(name = "store_industry_category")
    private String storeIndustryCategory;

    @Column(name = "store_industry_detail")
    private String storeIndustryDetail;

    @Column(name = "store_product_description")
    private String storeProductDescription;

    @Column(name = "store_count")
    private Integer storeCount;

    @Column(name = "store_average_price")
    private Integer storeAveragePrice;

    @Column(name = "store_bank_account")
    private String storeBankAccount;

    @Column(name = "store_receipt_name")
    private String storeReceiptName;

    @Column(name = "map_display_desired")
    private boolean mapDisplayDesired;

    @Column(name = "map_display_desired_date")
    private LocalDate mapDisplayDesiredDate;

    @Column(name = "store_latitude")
    private String storeLatitude;

    @Column(name = "store_longitude")
    private String storeLongitude;

    @Column(name = "business_hours1_start")
    private LocalTime businessHours1Start;

    @Column(name = "business_hours1_end")
    private LocalTime businessHours1End;

    @Column(name = "business_hours2_start")
    private LocalTime businessHours2Start;

    @Column(name = "business_hours2_end")
    private LocalTime businessHours2End;

    @Column(name = "closed_monday")
    private boolean closedMonday;

    @Column(name = "closed_tuesday")
    private boolean closedTuesday;

    @Column(name = "closed_wednesday")
    private boolean closedWednesday;

    @Column(name = "closed_thursday")
    private boolean closedThursday;

    @Column(name = "closed_friday")
    private boolean closedFriday;

    @Column(name = "closed_saturday")
    private boolean closedSaturday;

    @Column(name = "closed_sunday")
    private boolean closedSunday;

    @Column(name = "closed_holiday")
    private boolean closedHoliday;

    @Column(name = "closed_holiday_eve")
    private boolean closedHolidayEve;

    // 店舗住所
    @Column(name = "shop_zip_code")
    private String shopZipCode;

    @Column(name = "shop_prefecture")
    private String shopPrefecture;

    @Column(name = "shop_prefecture_kana")
    private String shopPrefectureKana;

    @Column(name = "shop_city")
    private String shopCity;

    @Column(name = "shop_city_kana")
    private String shopCityKana;

    @Column(name = "shop_town")
    private String shopTown;

    @Column(name = "shop_town_kana")
    private String shopTownKana;

    @Column(name = "shop_street_number")
    private String shopStreetNumber;

    @Column(name = "shop_street_number_kana")
    private String shopStreetNumberKana;

    @Column(name = "shop_building")
    private String shopBuilding;

    @Column(name = "shop_building_kana")
    private String shopBuildingKana;

    @Column(name = "shop_phone")
    private String shopPhone;

    @Column(name = "shop_business_permit_number")
    private String shopBusinessPermitNumber;

    @Column(name = "terminal_ic_status")
    private String terminalIcStatus;

    @Column(name = "terminal_possession_status")
    private String terminalPossessionStatus;

    // STEP 8: 発送申込
    @Column(name = "mpos_quantity")
    private Integer mposQuantity;

    @Column(name = "delivery_zip_code")
    private String deliveryZipCode;

    @Column(name = "delivery_prefecture")
    private String deliveryPrefecture;

    @Column(name = "delivery_prefecture_kana")
    private String deliveryPrefectureKana;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_city_kana")
    private String deliveryCityKana;

    @Column(name = "delivery_town")
    private String deliveryTown;

    @Column(name = "delivery_town_kana")
    private String deliveryTownKana;

    @Column(name = "delivery_street_number")
    private String deliveryStreetNumber;

    @Column(name = "delivery_street_number_kana")
    private String deliveryStreetNumberKana;

    @Column(name = "delivery_building")
    private String deliveryBuilding;

    @Column(name = "delivery_building_kana")
    private String deliveryBuildingKana;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "delivery_receiver")
    private String deliveryReceiver;

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public boolean isAgreedStarpay() {
        return agreedStarpay;
    }

    public void setAgreedStarpay(boolean agreedStarpay) {
        this.agreedStarpay = agreedStarpay;
    }

    public boolean isAgreedJcb() {
        return agreedJcb;
    }

    public void setAgreedJcb(boolean agreedJcb) {
        this.agreedJcb = agreedJcb;
    }

    public boolean isAgreedRyuginVisaMcCu() {
        return agreedRyuginVisaMcCu;
    }

    public void setAgreedRyuginVisaMcCu(boolean agreedRyuginVisaMcCu) {
        this.agreedRyuginVisaMcCu = agreedRyuginVisaMcCu;
    }

    public boolean isAgreedRyuginCuQr() {
        return agreedRyuginCuQr;
    }

    public void setAgreedRyuginCuQr(boolean agreedRyuginCuQr) {
        this.agreedRyuginCuQr = agreedRyuginCuQr;
    }

    public boolean isAgreedAgencyDelegation() {
        return agreedAgencyDelegation;
    }

    public void setAgreedAgencyDelegation(boolean agreedAgencyDelegation) {
        this.agreedAgencyDelegation = agreedAgencyDelegation;
    }

    public boolean isAgreedServiceTerms() {
        return agreedServiceTerms;
    }

    public void setAgreedServiceTerms(boolean agreedServiceTerms) {
        this.agreedServiceTerms = agreedServiceTerms;
    }

    public boolean isAgreedPrivacyPolicy() {
        return agreedPrivacyPolicy;
    }

    public void setAgreedPrivacyPolicy(boolean agreedPrivacyPolicy) {
        this.agreedPrivacyPolicy = agreedPrivacyPolicy;
    }

    public boolean isAgreedAuthorityConfirmed() {
        return agreedAuthorityConfirmed;
    }

    public void setAgreedAuthorityConfirmed(boolean agreedAuthorityConfirmed) {
        this.agreedAuthorityConfirmed = agreedAuthorityConfirmed;
    }

    public boolean isTxTypeVisitSales() {
        return txTypeVisitSales;
    }

    public void setTxTypeVisitSales(boolean txTypeVisitSales) {
        this.txTypeVisitSales = txTypeVisitSales;
    }

    public boolean isTxTypeContinuousService() {
        return txTypeContinuousService;
    }

    public void setTxTypeContinuousService(boolean txTypeContinuousService) {
        this.txTypeContinuousService = txTypeContinuousService;
    }

    public boolean isTxTypePhoneSolicitation() {
        return txTypePhoneSolicitation;
    }

    public void setTxTypePhoneSolicitation(boolean txTypePhoneSolicitation) {
        this.txTypePhoneSolicitation = txTypePhoneSolicitation;
    }

    public boolean isTxTypePrepaidService() {
        return txTypePrepaidService;
    }

    public void setTxTypePrepaidService(boolean txTypePrepaidService) {
        this.txTypePrepaidService = txTypePrepaidService;
    }

    public boolean isTxTypeBusinessInduction() {
        return txTypeBusinessInduction;
    }

    public void setTxTypeBusinessInduction(boolean txTypeBusinessInduction) {
        this.txTypeBusinessInduction = txTypeBusinessInduction;
    }

    public boolean isTxTypeChainSales() {
        return txTypeChainSales;
    }

    public void setTxTypeChainSales(boolean txTypeChainSales) {
        this.txTypeChainSales = txTypeChainSales;
    }

    public boolean isTxTypeNoneApplicable() {
        return txTypeNoneApplicable;
    }

    public void setTxTypeNoneApplicable(boolean txTypeNoneApplicable) {
        this.txTypeNoneApplicable = txTypeNoneApplicable;
    }

    public String getBusinessEntityType() {
        return businessEntityType;
    }

    public void setBusinessEntityType(String businessEntityType) {
        this.businessEntityType = businessEntityType;
    }

    public String getSalesFormat() {
        return salesFormat;
    }

    public void setSalesFormat(String salesFormat) {
        this.salesFormat = salesFormat;
    }

    public String getOperationFormat() {
        return operationFormat;
    }

    public void setOperationFormat(String operationFormat) {
        this.operationFormat = operationFormat;
    }

    public boolean isPayQrWechatPay() {
        return payQrWechatPay;
    }

    public void setPayQrWechatPay(boolean payQrWechatPay) {
        this.payQrWechatPay = payQrWechatPay;
    }

    public boolean isPayQrPaypay() {
        return payQrPaypay;
    }

    public void setPayQrPaypay(boolean payQrPaypay) {
        this.payQrPaypay = payQrPaypay;
    }

    public boolean isPayQrDBarai() {
        return payQrDBarai;
    }

    public void setPayQrDBarai(boolean payQrDBarai) {
        this.payQrDBarai = payQrDBarai;
    }

    public boolean isPayQrAuPay() {
        return payQrAuPay;
    }

    public void setPayQrAuPay(boolean payQrAuPay) {
        this.payQrAuPay = payQrAuPay;
    }

    public boolean isPayQrMerpay() {
        return payQrMerpay;
    }

    public void setPayQrMerpay(boolean payQrMerpay) {
        this.payQrMerpay = payQrMerpay;
    }

    public boolean isPayQrRakutenPay() {
        return payQrRakutenPay;
    }

    public void setPayQrRakutenPay(boolean payQrRakutenPay) {
        this.payQrRakutenPay = payQrRakutenPay;
    }

    public boolean isPayQrAlipayPlus() {
        return payQrAlipayPlus;
    }

    public void setPayQrAlipayPlus(boolean payQrAlipayPlus) {
        this.payQrAlipayPlus = payQrAlipayPlus;
    }

    public boolean isPayQrJkoPay() {
        return payQrJkoPay;
    }

    public void setPayQrJkoPay(boolean payQrJkoPay) {
        this.payQrJkoPay = payQrJkoPay;
    }

    public boolean isPayCreditJcb() {
        return payCreditJcb;
    }

    public void setPayCreditJcb(boolean payCreditJcb) {
        this.payCreditJcb = payCreditJcb;
    }

    public boolean isPayCreditDiscover() {
        return payCreditDiscover;
    }

    public void setPayCreditDiscover(boolean payCreditDiscover) {
        this.payCreditDiscover = payCreditDiscover;
    }

    public boolean isPayCreditVisa() {
        return payCreditVisa;
    }

    public void setPayCreditVisa(boolean payCreditVisa) {
        this.payCreditVisa = payCreditVisa;
    }

    public boolean isPayCreditMastercard() {
        return payCreditMastercard;
    }

    public void setPayCreditMastercard(boolean payCreditMastercard) {
        this.payCreditMastercard = payCreditMastercard;
    }

    public boolean isPayCreditDiners() {
        return payCreditDiners;
    }

    public void setPayCreditDiners(boolean payCreditDiners) {
        this.payCreditDiners = payCreditDiners;
    }

    public boolean isPayCreditAmex() {
        return payCreditAmex;
    }

    public void setPayCreditAmex(boolean payCreditAmex) {
        this.payCreditAmex = payCreditAmex;
    }

    public String getPayCreditBonus() {
        return payCreditBonus;
    }

    public void setPayCreditBonus(String payCreditBonus) {
        this.payCreditBonus = payCreditBonus;
    }

    public String getPayCreditTwoTimes() {
        return payCreditTwoTimes;
    }

    public void setPayCreditTwoTimes(String payCreditTwoTimes) {
        this.payCreditTwoTimes = payCreditTwoTimes;
    }

    public String getPayCreditInstallment() {
        return payCreditInstallment;
    }

    public void setPayCreditInstallment(String payCreditInstallment) {
        this.payCreditInstallment = payCreditInstallment;
    }

    public String getPayCreditRevolving() {
        return payCreditRevolving;
    }

    public void setPayCreditRevolving(String payCreditRevolving) {
        this.payCreditRevolving = payCreditRevolving;
    }

    public boolean isPayEmoneyId() {
        return payEmoneyId;
    }

    public void setPayEmoneyId(boolean payEmoneyId) {
        this.payEmoneyId = payEmoneyId;
    }

    public boolean isPayEmoneyWaon() {
        return payEmoneyWaon;
    }

    public void setPayEmoneyWaon(boolean payEmoneyWaon) {
        this.payEmoneyWaon = payEmoneyWaon;
    }

    public boolean isPayEmoneyRakutenEdy() {
        return payEmoneyRakutenEdy;
    }

    public void setPayEmoneyRakutenEdy(boolean payEmoneyRakutenEdy) {
        this.payEmoneyRakutenEdy = payEmoneyRakutenEdy;
    }

    public boolean isPayEmoneyNanaco() {
        return payEmoneyNanaco;
    }

    public void setPayEmoneyNanaco(boolean payEmoneyNanaco) {
        this.payEmoneyNanaco = payEmoneyNanaco;
    }

    public boolean isPayEmoneyTransitIc() {
        return payEmoneyTransitIc;
    }

    public void setPayEmoneyTransitIc(boolean payEmoneyTransitIc) {
        this.payEmoneyTransitIc = payEmoneyTransitIc;
    }

    public boolean isPayEmoneyQuickPay() {
        return payEmoneyQuickPay;
    }

    public void setPayEmoneyQuickPay(boolean payEmoneyQuickPay) {
        this.payEmoneyQuickPay = payEmoneyQuickPay;
    }

    public boolean isPayEmoneyApplePay() {
        return payEmoneyApplePay;
    }

    public void setPayEmoneyApplePay(boolean payEmoneyApplePay) {
        this.payEmoneyApplePay = payEmoneyApplePay;
    }

    public String getCorporateNumber() {
        return corporateNumber;
    }

    public void setCorporateNumber(String corporateNumber) {
        this.corporateNumber = corporateNumber;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public String getCorporateNameKana() {
        return corporateNameKana;
    }

    public void setCorporateNameKana(String corporateNameKana) {
        this.corporateNameKana = corporateNameKana;
    }

    public String getCorporateNameEn() {
        return corporateNameEn;
    }

    public void setCorporateNameEn(String corporateNameEn) {
        this.corporateNameEn = corporateNameEn;
    }

    public LocalDate getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(LocalDate establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public String getCorporateType() {
        return corporateType;
    }

    public void setCorporateType(String corporateType) {
        this.corporateType = corporateType;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandNameKana() {
        return brandNameKana;
    }

    public void setBrandNameKana(String brandNameKana) {
        this.brandNameKana = brandNameKana;
    }

    public String getBrandNameEn() {
        return brandNameEn;
    }

    public void setBrandNameEn(String brandNameEn) {
        this.brandNameEn = brandNameEn;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    public Long getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(Long annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public Long getCapitalAmount() {
        return capitalAmount;
    }

    public void setCapitalAmount(Long capitalAmount) {
        this.capitalAmount = capitalAmount;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getIndustryCategory() {
        return industryCategory;
    }

    public void setIndustryCategory(String industryCategory) {
        this.industryCategory = industryCategory;
    }

    public String getIndustryDetail() {
        return industryDetail;
    }

    public void setIndustryDetail(String industryDetail) {
        this.industryDetail = industryDetail;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getCompanyZipCode() {
        return companyZipCode;
    }

    public void setCompanyZipCode(String companyZipCode) {
        this.companyZipCode = companyZipCode;
    }

    public String getCompanyPrefecture() {
        return companyPrefecture;
    }

    public void setCompanyPrefecture(String companyPrefecture) {
        this.companyPrefecture = companyPrefecture;
    }

    public String getCompanyPrefectureKana() {
        return companyPrefectureKana;
    }

    public void setCompanyPrefectureKana(String companyPrefectureKana) {
        this.companyPrefectureKana = companyPrefectureKana;
    }

    public String getCompanyCity() {
        return companyCity;
    }

    public void setCompanyCity(String companyCity) {
        this.companyCity = companyCity;
    }

    public String getCompanyCityKana() {
        return companyCityKana;
    }

    public void setCompanyCityKana(String companyCityKana) {
        this.companyCityKana = companyCityKana;
    }

    public String getCompanyTown() {
        return companyTown;
    }

    public void setCompanyTown(String companyTown) {
        this.companyTown = companyTown;
    }

    public String getCompanyTownKana() {
        return companyTownKana;
    }

    public void setCompanyTownKana(String companyTownKana) {
        this.companyTownKana = companyTownKana;
    }

    public String getCompanyStreetNumber() {
        return companyStreetNumber;
    }

    public void setCompanyStreetNumber(String companyStreetNumber) {
        this.companyStreetNumber = companyStreetNumber;
    }

    public String getCompanyStreetNumberKana() {
        return companyStreetNumberKana;
    }

    public void setCompanyStreetNumberKana(String companyStreetNumberKana) {
        this.companyStreetNumberKana = companyStreetNumberKana;
    }

    public String getCompanyBuilding() {
        return companyBuilding;
    }

    public void setCompanyBuilding(String companyBuilding) {
        this.companyBuilding = companyBuilding;
    }

    public String getCompanyBuildingKana() {
        return companyBuildingKana;
    }

    public void setCompanyBuildingKana(String companyBuildingKana) {
        this.companyBuildingKana = companyBuildingKana;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyFax() {
        return companyFax;
    }

    public void setCompanyFax(String companyFax) {
        this.companyFax = companyFax;
    }

    public String getCompanyMobile() {
        return companyMobile;
    }

    public void setCompanyMobile(String companyMobile) {
        this.companyMobile = companyMobile;
    }

    public String getRepLastName() {
        return repLastName;
    }

    public void setRepLastName(String repLastName) {
        this.repLastName = repLastName;
    }

    public String getRepLastNameKana() {
        return repLastNameKana;
    }

    public void setRepLastNameKana(String repLastNameKana) {
        this.repLastNameKana = repLastNameKana;
    }

    public String getRepLastNameEn() {
        return repLastNameEn;
    }

    public void setRepLastNameEn(String repLastNameEn) {
        this.repLastNameEn = repLastNameEn;
    }

    public String getRepFirstName() {
        return repFirstName;
    }

    public void setRepFirstName(String repFirstName) {
        this.repFirstName = repFirstName;
    }

    public String getRepFirstNameKana() {
        return repFirstNameKana;
    }

    public void setRepFirstNameKana(String repFirstNameKana) {
        this.repFirstNameKana = repFirstNameKana;
    }

    public String getRepFirstNameEn() {
        return repFirstNameEn;
    }

    public void setRepFirstNameEn(String repFirstNameEn) {
        this.repFirstNameEn = repFirstNameEn;
    }

    public LocalDate getRepBirthDate() {
        return repBirthDate;
    }

    public void setRepBirthDate(LocalDate repBirthDate) {
        this.repBirthDate = repBirthDate;
    }

    public String getRepGender() {
        return repGender;
    }

    public void setRepGender(String repGender) {
        this.repGender = repGender;
    }

    public String getRepZipCode() {
        return repZipCode;
    }

    public void setRepZipCode(String repZipCode) {
        this.repZipCode = repZipCode;
    }

    public String getRepPrefecture() {
        return repPrefecture;
    }

    public void setRepPrefecture(String repPrefecture) {
        this.repPrefecture = repPrefecture;
    }

    public String getRepPrefectureKana() {
        return repPrefectureKana;
    }

    public void setRepPrefectureKana(String repPrefectureKana) {
        this.repPrefectureKana = repPrefectureKana;
    }

    public String getRepCity() {
        return repCity;
    }

    public void setRepCity(String repCity) {
        this.repCity = repCity;
    }

    public String getRepCityKana() {
        return repCityKana;
    }

    public void setRepCityKana(String repCityKana) {
        this.repCityKana = repCityKana;
    }

    public String getRepTown() {
        return repTown;
    }

    public void setRepTown(String repTown) {
        this.repTown = repTown;
    }

    public String getRepTownKana() {
        return repTownKana;
    }

    public void setRepTownKana(String repTownKana) {
        this.repTownKana = repTownKana;
    }

    public String getRepStreetNumber() {
        return repStreetNumber;
    }

    public void setRepStreetNumber(String repStreetNumber) {
        this.repStreetNumber = repStreetNumber;
    }

    public String getRepStreetNumberKana() {
        return repStreetNumberKana;
    }

    public void setRepStreetNumberKana(String repStreetNumberKana) {
        this.repStreetNumberKana = repStreetNumberKana;
    }

    public String getRepBuilding() {
        return repBuilding;
    }

    public void setRepBuilding(String repBuilding) {
        this.repBuilding = repBuilding;
    }

    public String getRepPhone() {
        return repPhone;
    }

    public void setRepPhone(String repPhone) {
        this.repPhone = repPhone;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactLastNameKana() {
        return contactLastNameKana;
    }

    public void setContactLastNameKana(String contactLastNameKana) {
        this.contactLastNameKana = contactLastNameKana;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactFirstNameKana() {
        return contactFirstNameKana;
    }

    public void setContactFirstNameKana(String contactFirstNameKana) {
        this.contactFirstNameKana = contactFirstNameKana;
    }

    public String getContactZipCode() {
        return contactZipCode;
    }

    public void setContactZipCode(String contactZipCode) {
        this.contactZipCode = contactZipCode;
    }

    public String getContactPrefecture() {
        return contactPrefecture;
    }

    public void setContactPrefecture(String contactPrefecture) {
        this.contactPrefecture = contactPrefecture;
    }

    public String getContactPrefectureKana() {
        return contactPrefectureKana;
    }

    public void setContactPrefectureKana(String contactPrefectureKana) {
        this.contactPrefectureKana = contactPrefectureKana;
    }

    public String getContactCity() {
        return contactCity;
    }

    public void setContactCity(String contactCity) {
        this.contactCity = contactCity;
    }

    public String getContactCityKana() {
        return contactCityKana;
    }

    public void setContactCityKana(String contactCityKana) {
        this.contactCityKana = contactCityKana;
    }

    public String getContactTown() {
        return contactTown;
    }

    public void setContactTown(String contactTown) {
        this.contactTown = contactTown;
    }

    public String getContactTownKana() {
        return contactTownKana;
    }

    public void setContactTownKana(String contactTownKana) {
        this.contactTownKana = contactTownKana;
    }

    public String getContactStreetNumber() {
        return contactStreetNumber;
    }

    public void setContactStreetNumber(String contactStreetNumber) {
        this.contactStreetNumber = contactStreetNumber;
    }

    public String getContactStreetNumberKana() {
        return contactStreetNumberKana;
    }

    public void setContactStreetNumberKana(String contactStreetNumberKana) {
        this.contactStreetNumberKana = contactStreetNumberKana;
    }

    public String getContactBuilding() {
        return contactBuilding;
    }

    public void setContactBuilding(String contactBuilding) {
        this.contactBuilding = contactBuilding;
    }

    public String getContactBuildingKana() {
        return contactBuildingKana;
    }

    public void setContactBuildingKana(String contactBuildingKana) {
        this.contactBuildingKana = contactBuildingKana;
    }

    public String getContactDepartment() {
        return contactDepartment;
    }

    public void setContactDepartment(String contactDepartment) {
        this.contactDepartment = contactDepartment;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone1() {
        return contactPhone1;
    }

    public void setContactPhone1(String contactPhone1) {
        this.contactPhone1 = contactPhone1;
    }

    public String getContactPhone2() {
        return contactPhone2;
    }

    public void setContactPhone2(String contactPhone2) {
        this.contactPhone2 = contactPhone2;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderKana() {
        return accountHolderKana;
    }

    public void setAccountHolderKana(String accountHolderKana) {
        this.accountHolderKana = accountHolderKana;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreNameKana() {
        return storeNameKana;
    }

    public void setStoreNameKana(String storeNameKana) {
        this.storeNameKana = storeNameKana;
    }

    public String getStoreNameEn() {
        return storeNameEn;
    }

    public void setStoreNameEn(String storeNameEn) {
        this.storeNameEn = storeNameEn;
    }

    public String getStoreBrandName() {
        return storeBrandName;
    }

    public void setStoreBrandName(String storeBrandName) {
        this.storeBrandName = storeBrandName;
    }

    public String getStoreBrandNameKana() {
        return storeBrandNameKana;
    }

    public void setStoreBrandNameKana(String storeBrandNameKana) {
        this.storeBrandNameKana = storeBrandNameKana;
    }

    public String getStoreBrandNameEn() {
        return storeBrandNameEn;
    }

    public void setStoreBrandNameEn(String storeBrandNameEn) {
        this.storeBrandNameEn = storeBrandNameEn;
    }

    public String getStoreIndustryCategory() {
        return storeIndustryCategory;
    }

    public void setStoreIndustryCategory(String storeIndustryCategory) {
        this.storeIndustryCategory = storeIndustryCategory;
    }

    public String getStoreIndustryDetail() {
        return storeIndustryDetail;
    }

    public void setStoreIndustryDetail(String storeIndustryDetail) {
        this.storeIndustryDetail = storeIndustryDetail;
    }

    public String getStoreProductDescription() {
        return storeProductDescription;
    }

    public void setStoreProductDescription(String storeProductDescription) {
        this.storeProductDescription = storeProductDescription;
    }

    public Integer getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(Integer storeCount) {
        this.storeCount = storeCount;
    }

    public Integer getStoreAveragePrice() {
        return storeAveragePrice;
    }

    public void setStoreAveragePrice(Integer storeAveragePrice) {
        this.storeAveragePrice = storeAveragePrice;
    }

    public String getStoreBankAccount() {
        return storeBankAccount;
    }

    public void setStoreBankAccount(String storeBankAccount) {
        this.storeBankAccount = storeBankAccount;
    }

    public String getStoreReceiptName() {
        return storeReceiptName;
    }

    public void setStoreReceiptName(String storeReceiptName) {
        this.storeReceiptName = storeReceiptName;
    }

    public boolean isMapDisplayDesired() {
        return mapDisplayDesired;
    }

    public void setMapDisplayDesired(boolean mapDisplayDesired) {
        this.mapDisplayDesired = mapDisplayDesired;
    }

    public LocalDate getMapDisplayDesiredDate() {
        return mapDisplayDesiredDate;
    }

    public void setMapDisplayDesiredDate(LocalDate mapDisplayDesiredDate) {
        this.mapDisplayDesiredDate = mapDisplayDesiredDate;
    }

    public String getStoreLatitude() {
        return storeLatitude;
    }

    public void setStoreLatitude(String storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public String getStoreLongitude() {
        return storeLongitude;
    }

    public void setStoreLongitude(String storeLongitude) {
        this.storeLongitude = storeLongitude;
    }

    public LocalTime getBusinessHours1Start() {
        return businessHours1Start;
    }

    public void setBusinessHours1Start(LocalTime businessHours1Start) {
        this.businessHours1Start = businessHours1Start;
    }

    public LocalTime getBusinessHours1End() {
        return businessHours1End;
    }

    public void setBusinessHours1End(LocalTime businessHours1End) {
        this.businessHours1End = businessHours1End;
    }

    public LocalTime getBusinessHours2Start() {
        return businessHours2Start;
    }

    public void setBusinessHours2Start(LocalTime businessHours2Start) {
        this.businessHours2Start = businessHours2Start;
    }

    public LocalTime getBusinessHours2End() {
        return businessHours2End;
    }

    public void setBusinessHours2End(LocalTime businessHours2End) {
        this.businessHours2End = businessHours2End;
    }

    public boolean isClosedMonday() {
        return closedMonday;
    }

    public void setClosedMonday(boolean closedMonday) {
        this.closedMonday = closedMonday;
    }

    public boolean isClosedTuesday() {
        return closedTuesday;
    }

    public void setClosedTuesday(boolean closedTuesday) {
        this.closedTuesday = closedTuesday;
    }

    public boolean isClosedWednesday() {
        return closedWednesday;
    }

    public void setClosedWednesday(boolean closedWednesday) {
        this.closedWednesday = closedWednesday;
    }

    public boolean isClosedThursday() {
        return closedThursday;
    }

    public void setClosedThursday(boolean closedThursday) {
        this.closedThursday = closedThursday;
    }

    public boolean isClosedFriday() {
        return closedFriday;
    }

    public void setClosedFriday(boolean closedFriday) {
        this.closedFriday = closedFriday;
    }

    public boolean isClosedSaturday() {
        return closedSaturday;
    }

    public void setClosedSaturday(boolean closedSaturday) {
        this.closedSaturday = closedSaturday;
    }

    public boolean isClosedSunday() {
        return closedSunday;
    }

    public void setClosedSunday(boolean closedSunday) {
        this.closedSunday = closedSunday;
    }

    public boolean isClosedHoliday() {
        return closedHoliday;
    }

    public void setClosedHoliday(boolean closedHoliday) {
        this.closedHoliday = closedHoliday;
    }

    public boolean isClosedHolidayEve() {
        return closedHolidayEve;
    }

    public void setClosedHolidayEve(boolean closedHolidayEve) {
        this.closedHolidayEve = closedHolidayEve;
    }

    public String getShopZipCode() {
        return shopZipCode;
    }

    public void setShopZipCode(String shopZipCode) {
        this.shopZipCode = shopZipCode;
    }

    public String getShopPrefecture() {
        return shopPrefecture;
    }

    public void setShopPrefecture(String shopPrefecture) {
        this.shopPrefecture = shopPrefecture;
    }

    public String getShopPrefectureKana() {
        return shopPrefectureKana;
    }

    public void setShopPrefectureKana(String shopPrefectureKana) {
        this.shopPrefectureKana = shopPrefectureKana;
    }

    public String getShopCity() {
        return shopCity;
    }

    public void setShopCity(String shopCity) {
        this.shopCity = shopCity;
    }

    public String getShopCityKana() {
        return shopCityKana;
    }

    public void setShopCityKana(String shopCityKana) {
        this.shopCityKana = shopCityKana;
    }

    public String getShopTown() {
        return shopTown;
    }

    public void setShopTown(String shopTown) {
        this.shopTown = shopTown;
    }

    public String getShopTownKana() {
        return shopTownKana;
    }

    public void setShopTownKana(String shopTownKana) {
        this.shopTownKana = shopTownKana;
    }

    public String getShopStreetNumber() {
        return shopStreetNumber;
    }

    public void setShopStreetNumber(String shopStreetNumber) {
        this.shopStreetNumber = shopStreetNumber;
    }

    public String getShopStreetNumberKana() {
        return shopStreetNumberKana;
    }

    public void setShopStreetNumberKana(String shopStreetNumberKana) {
        this.shopStreetNumberKana = shopStreetNumberKana;
    }

    public String getShopBuilding() {
        return shopBuilding;
    }

    public void setShopBuilding(String shopBuilding) {
        this.shopBuilding = shopBuilding;
    }

    public String getShopBuildingKana() {
        return shopBuildingKana;
    }

    public void setShopBuildingKana(String shopBuildingKana) {
        this.shopBuildingKana = shopBuildingKana;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public String getShopBusinessPermitNumber() {
        return shopBusinessPermitNumber;
    }

    public void setShopBusinessPermitNumber(String shopBusinessPermitNumber) {
        this.shopBusinessPermitNumber = shopBusinessPermitNumber;
    }

    public String getTerminalIcStatus() {
        return terminalIcStatus;
    }

    public void setTerminalIcStatus(String terminalIcStatus) {
        this.terminalIcStatus = terminalIcStatus;
    }

    public String getTerminalPossessionStatus() {
        return terminalPossessionStatus;
    }

    public void setTerminalPossessionStatus(String terminalPossessionStatus) {
        this.terminalPossessionStatus = terminalPossessionStatus;
    }

    public Integer getMposQuantity() {
        return mposQuantity;
    }

    public void setMposQuantity(Integer mposQuantity) {
        this.mposQuantity = mposQuantity;
    }

    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getDeliveryPrefecture() {
        return deliveryPrefecture;
    }

    public void setDeliveryPrefecture(String deliveryPrefecture) {
        this.deliveryPrefecture = deliveryPrefecture;
    }

    public String getDeliveryPrefectureKana() {
        return deliveryPrefectureKana;
    }

    public void setDeliveryPrefectureKana(String deliveryPrefectureKana) {
        this.deliveryPrefectureKana = deliveryPrefectureKana;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryCityKana() {
        return deliveryCityKana;
    }

    public void setDeliveryCityKana(String deliveryCityKana) {
        this.deliveryCityKana = deliveryCityKana;
    }

    public String getDeliveryTown() {
        return deliveryTown;
    }

    public void setDeliveryTown(String deliveryTown) {
        this.deliveryTown = deliveryTown;
    }

    public String getDeliveryTownKana() {
        return deliveryTownKana;
    }

    public void setDeliveryTownKana(String deliveryTownKana) {
        this.deliveryTownKana = deliveryTownKana;
    }

    public String getDeliveryStreetNumber() {
        return deliveryStreetNumber;
    }

    public void setDeliveryStreetNumber(String deliveryStreetNumber) {
        this.deliveryStreetNumber = deliveryStreetNumber;
    }

    public String getDeliveryStreetNumberKana() {
        return deliveryStreetNumberKana;
    }

    public void setDeliveryStreetNumberKana(String deliveryStreetNumberKana) {
        this.deliveryStreetNumberKana = deliveryStreetNumberKana;
    }

    public String getDeliveryBuilding() {
        return deliveryBuilding;
    }

    public void setDeliveryBuilding(String deliveryBuilding) {
        this.deliveryBuilding = deliveryBuilding;
    }

    public String getDeliveryBuildingKana() {
        return deliveryBuildingKana;
    }

    public void setDeliveryBuildingKana(String deliveryBuildingKana) {
        this.deliveryBuildingKana = deliveryBuildingKana;
    }

    public String getDeliveryPhone() {
        return deliveryPhone;
    }

    public void setDeliveryPhone(String deliveryPhone) {
        this.deliveryPhone = deliveryPhone;
    }

    public String getDeliveryReceiver() {
        return deliveryReceiver;
    }

    public void setDeliveryReceiver(String deliveryReceiver) {
        this.deliveryReceiver = deliveryReceiver;
    }

    @Column(name = "temp_password_hash")
    private String tempPasswordHash;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "password_set_flg")
    private boolean passwordSetFlg;

    @Column(name = "delete_flag")
    private boolean deleteFlag;

    public String getTempPasswordHash() {
        return tempPasswordHash;
    }

    public void setTempPasswordHash(String tempPasswordHash) {
        this.tempPasswordHash = tempPasswordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isPasswordSetFlg() {
        return passwordSetFlg;
    }

    public void setPasswordSetFlg(boolean passwordSetFlg) {
        this.passwordSetFlg = passwordSetFlg;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return "MerchantApplication{"
                + "memberCode=" + memberCode
                + ", applicationStatus=" + applicationStatus
                + ", corporateName=" + corporateName
                + "}";
    }
}
