package jp.co.dragonagency.dapaycore.dto;

/**
 * 加盟店申込フォームの全入力値を受け取るリクエスト DTO。
 * multipart/form-data の "data" パートとして JSON で受信する。
 */
public class MerchantApplicationRequest {

    // STEP 1: 事前確認
    private boolean agreedStarpay;
    private boolean agreedJcb;
    private boolean agreedRyuginVisaMcCu;
    private boolean agreedRyuginCuQr;
    private boolean agreedAgencyDelegation;
    private boolean agreedServiceTerms;
    private boolean agreedPrivacyPolicy;
    private boolean agreedAuthorityConfirmed;

    // STEP 2: 取引形態
    private boolean txTypeVisitSales;
    private boolean txTypeContinuousService;
    private boolean txTypePhoneSolicitation;
    private boolean txTypePrepaidService;
    private boolean txTypeBusinessInduction;
    private boolean txTypeChainSales;
    private boolean txTypeNoneApplicable;
    private String businessEntityType;
    private String salesFormat;
    private String operationFormat;

    // STEP 3: 決済種類（QR）
    private boolean payQrWechatPay;
    private boolean payQrPaypay;
    private boolean payQrDBarai;
    private boolean payQrAuPay;
    private boolean payQrMerpay;
    private boolean payQrRakutenPay;
    private boolean payQrAlipayPlus;
    private boolean payQrJkoPay;
    // STEP 3: 決済種類（クレジット）
    private boolean payCreditJcb;
    private boolean payCreditDiscover;
    private boolean payCreditVisa;
    private boolean payCreditMastercard;
    private boolean payCreditDiners;
    private boolean payCreditAmex;
    private String payCreditBonus;
    private String payCreditTwoTimes;
    private String payCreditInstallment;
    private String payCreditRevolving;
    // STEP 3: 決済種類（電子マネー）
    private boolean payEmoneyId;
    private boolean payEmoneyWaon;
    private boolean payEmoneyRakutenEdy;
    private boolean payEmoneyNanaco;
    private boolean payEmoneyTransitIc;

    // STEP 4: 法人情報
    private String corporateNumber;
    private String corporateName;
    private String corporateNameKana;
    private String corporateNameEn;
    private String establishmentDate;
    private String corporateType;
    private String brandName;
    private String brandNameKana;
    private String brandNameEn;
    private String companyUrl;
    private String annualRevenue;
    private String capitalAmount;
    private String employeeCount;
    private String industryCategory;
    private String industryDetail;
    private String businessDescription;
    // 本社住所
    private String companyZipCode;
    private String companyPrefecture;
    private String companyPrefectureKana;
    private String companyCity;
    private String companyCityKana;
    private String companyTown;
    private String companyTownKana;
    private String companyStreetNumber;
    private String companyStreetNumberKana;
    private String companyBuilding;
    private String companyBuildingKana;
    private String companyPhone;
    private String companyFax;
    private String companyMobile;
    // 代表者情報
    private String repLastName;
    private String repLastNameKana;
    private String repLastNameEn;
    private String repFirstName;
    private String repFirstNameKana;
    private String repFirstNameEn;
    private String repBirthDate;
    private String repGender;
    // 代表者自宅住所
    private String repZipCode;
    private String repPrefecture;
    private String repPrefectureKana;
    private String repCity;
    private String repCityKana;
    private String repTown;
    private String repTownKana;
    private String repStreetNumber;
    private String repStreetNumberKana;
    private String repBuilding;
    private String repPhone;
    // 担当者情報
    private String contactLastName;
    private String contactLastNameKana;
    private String contactFirstName;
    private String contactFirstNameKana;
    // 担当者勤務先住所
    private String contactZipCode;
    private String contactPrefecture;
    private String contactPrefectureKana;
    private String contactCity;
    private String contactCityKana;
    private String contactTown;
    private String contactTownKana;
    private String contactStreetNumber;
    private String contactStreetNumberKana;
    private String contactBuilding;
    private String contactBuildingKana;
    private String contactDepartment;
    private String contactEmail;
    private String contactPhone1;
    private String contactPhone2;

    // STEP 5: 口座情報
    private String bankCode;
    private String bankName;
    private String branchCode;
    private String branchName;
    private String accountType;
    private String accountNumber;
    private String accountHolderKana;

    // STEP 6: 店舗情報
    private String storeName;
    private String storeNameKana;
    private String storeNameEn;
    private String storeBrandName;
    private String storeBrandNameKana;
    private String storeBrandNameEn;
    private String storeIndustryCategory;
    private String storeIndustryDetail;
    private String storeProductDescription;
    private String storeCount;
    private String storeAveragePrice;
    private String storeBankAccount;
    private String storeReceiptName;
    // 店舗住所
    private String shopZipCode;
    private String shopPrefecture;
    private String shopPrefectureKana;
    private String shopCity;
    private String shopCityKana;
    private String shopTown;
    private String shopTownKana;
    private String shopStreetNumber;
    private String shopStreetNumberKana;
    private String shopBuilding;
    private String shopBuildingKana;
    private String shopPhone;
    // 端末情報
    private String terminalIcStatus;
    private String terminalPossessionStatus;
    // 地図・営業時間・定休日・営業許可番号
    private String storeLatitude;
    private String storeLongitude;
    private String businessHours1Start;
    private String businessHours1End;
    private String businessHours2Start;
    private String businessHours2End;
    private boolean closedSunday;
    private boolean closedHoliday;
    private boolean closedHolidayEve;
    private String shopBusinessPermitNumber;

    // STEP 8: 発送申込
    private String mposQuantity;
    private String deliveryZipCode;
    private String deliveryPrefecture;
    private String deliveryPrefectureKana;
    private String deliveryCity;
    private String deliveryCityKana;
    private String deliveryTown;
    private String deliveryTownKana;
    private String deliveryStreetNumber;
    private String deliveryStreetNumberKana;
    private String deliveryBuilding;
    private String deliveryBuildingKana;
    private String deliveryPhone;
    private String deliveryReceiver;

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

    public String getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(String establishmentDate) {
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

    public String getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(String annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public String getCapitalAmount() {
        return capitalAmount;
    }

    public void setCapitalAmount(String capitalAmount) {
        this.capitalAmount = capitalAmount;
    }

    public String getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(String employeeCount) {
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

    public String getRepBirthDate() {
        return repBirthDate;
    }

    public void setRepBirthDate(String repBirthDate) {
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

    public String getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(String storeCount) {
        this.storeCount = storeCount;
    }

    public String getStoreAveragePrice() {
        return storeAveragePrice;
    }

    public void setStoreAveragePrice(String storeAveragePrice) {
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

    public String getMposQuantity() {
        return mposQuantity;
    }

    public void setMposQuantity(String mposQuantity) {
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

    public String getBusinessHours1Start() {
        return businessHours1Start;
    }

    public void setBusinessHours1Start(String businessHours1Start) {
        this.businessHours1Start = businessHours1Start;
    }

    public String getBusinessHours1End() {
        return businessHours1End;
    }

    public void setBusinessHours1End(String businessHours1End) {
        this.businessHours1End = businessHours1End;
    }

    public String getBusinessHours2Start() {
        return businessHours2Start;
    }

    public void setBusinessHours2Start(String businessHours2Start) {
        this.businessHours2Start = businessHours2Start;
    }

    public String getBusinessHours2End() {
        return businessHours2End;
    }

    public void setBusinessHours2End(String businessHours2End) {
        this.businessHours2End = businessHours2End;
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

    public String getShopBusinessPermitNumber() {
        return shopBusinessPermitNumber;
    }

    public void setShopBusinessPermitNumber(String shopBusinessPermitNumber) {
        this.shopBusinessPermitNumber = shopBusinessPermitNumber;
    }
}
