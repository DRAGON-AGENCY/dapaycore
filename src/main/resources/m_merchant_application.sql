-- 加盟店申込。申込ウィザード（9ステップ）の全入力項目を1レコードで保持する。
CREATE TABLE IF NOT EXISTS m_merchant_application (
    member_code                     VARCHAR(20)   NOT NULL,

    -- ステータス管理。application_status: UNREVIEWED / REVIEWING / APPROVED / REJECTED
    application_status              VARCHAR(20)   NOT NULL DEFAULT 'UNREVIEWED',
    submitted_at                    TIMESTAMP,
    created_at                      TIMESTAMP     NOT NULL,
    updated_at                      TIMESTAMP     NOT NULL,
    update_user_id                  VARCHAR(20),

    -- STEP 1: 事前確認（規約同意）
    agreed_starpay                  BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_jcb                      BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_ryugin_visa_mc_cu        BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_ryugin_cu_qr             BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_agency_delegation        BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_service_terms            BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_privacy_policy           BOOLEAN       NOT NULL DEFAULT FALSE,
    agreed_authority_confirmed      BOOLEAN       NOT NULL DEFAULT FALSE,

    -- STEP 2: 取引形態（特定商取引法）
    tx_type_visit_sales             BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_continuous_service      BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_phone_solicitation      BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_prepaid_service         BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_business_induction      BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_chain_sales             BOOLEAN       NOT NULL DEFAULT FALSE,
    tx_type_none_applicable         BOOLEAN       NOT NULL DEFAULT FALSE,
    -- STEP 2: 事業区分。business_entity_type: 法人 / 個人事業主
    business_entity_type            VARCHAR(20),
    -- sales_format: 固定店舗 / 移動販売
    sales_format                    VARCHAR(20),
    -- operation_format: 個店 / 商業施設 / フランチャイズ / 該当しない
    operation_format                VARCHAR(20),

    -- STEP 3: 決済種類（QRコード決済）
    pay_qr_wechat_pay               BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_paypay                   BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_d_barai                  BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_au_pay                   BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_merpay                   BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_rakuten_pay              BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_alipay_plus              BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_qr_jkopay                   BOOLEAN       NOT NULL DEFAULT FALSE,
    -- STEP 3: 決済種類（クレジットカード）
    pay_credit_jcb                  BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_credit_discover             BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_credit_visa                 BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_credit_mastercard           BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_credit_diners               BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_credit_amex                 BOOLEAN       NOT NULL DEFAULT FALSE,
    -- pay_credit_*: する / しない
    pay_credit_bonus                VARCHAR(10),
    pay_credit_two_times            VARCHAR(10),
    pay_credit_installment          VARCHAR(10),
    pay_credit_revolving            VARCHAR(10),
    -- STEP 3: 決済種類（電子マネー）
    pay_emoney_id                   BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_emoney_waon                 BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_emoney_rakuten_edy          BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_emoney_nanaco               BOOLEAN       NOT NULL DEFAULT FALSE,
    pay_emoney_transit_ic           BOOLEAN       NOT NULL DEFAULT FALSE,

    -- STEP 4: 法人情報（申込者情報）
    corporate_number                VARCHAR(13),
    corporate_name                  VARCHAR(200),
    corporate_name_kana             VARCHAR(400),
    corporate_name_en               VARCHAR(400),
    establishment_date              DATE,
    -- corporate_type: 株式会社 / 合同会社 / 有限会社 / 合名会社 / 合資会社 / 一般社団法人 / その他
    corporate_type                  VARCHAR(30),
    brand_name                      VARCHAR(200),
    brand_name_kana                 VARCHAR(400),
    brand_name_en                   VARCHAR(400),
    company_url                     VARCHAR(500),
    annual_revenue                  BIGINT,
    capital_amount                  BIGINT,
    employee_count                  INTEGER,
    industry_category               VARCHAR(100),
    industry_detail                 VARCHAR(100),
    business_description            VARCHAR(1000),
    -- STEP 4: 本社住所（個人事業主は本人確認書類記載の住所）
    company_zip_code                VARCHAR(8),
    company_prefecture              VARCHAR(20),
    company_prefecture_kana         VARCHAR(100),
    company_city                    VARCHAR(100),
    company_city_kana               VARCHAR(200),
    company_town                    VARCHAR(100),
    company_town_kana               VARCHAR(200),
    company_street_number           VARCHAR(100),
    company_street_number_kana      VARCHAR(200),
    company_building                VARCHAR(200),
    company_building_kana           VARCHAR(200),
    company_phone                   VARCHAR(20),
    company_fax                     VARCHAR(20),
    company_mobile                  VARCHAR(20),
    -- STEP 4: 代表者情報
    rep_last_name                   VARCHAR(100),
    rep_last_name_kana              VARCHAR(200),
    rep_last_name_en                VARCHAR(200),
    rep_first_name                  VARCHAR(100),
    rep_first_name_kana             VARCHAR(200),
    rep_first_name_en               VARCHAR(200),
    rep_birth_date                  DATE,
    -- rep_gender: 男 / 女
    rep_gender                      VARCHAR(5),
    -- STEP 4: 代表者自宅住所
    rep_zip_code                    VARCHAR(8),
    rep_prefecture                  VARCHAR(20),
    rep_prefecture_kana             VARCHAR(100),
    rep_city                        VARCHAR(100),
    rep_city_kana                   VARCHAR(200),
    rep_town                        VARCHAR(100),
    rep_town_kana                   VARCHAR(200),
    rep_street_number               VARCHAR(100),
    rep_street_number_kana          VARCHAR(200),
    rep_building                    VARCHAR(200),
    rep_phone                       VARCHAR(20),
    -- STEP 4: 担当者情報
    contact_last_name               VARCHAR(100),
    contact_last_name_kana          VARCHAR(200),
    contact_first_name              VARCHAR(100),
    contact_first_name_kana         VARCHAR(200),
    -- STEP 4: 担当者勤務先住所
    contact_zip_code                VARCHAR(8),
    contact_prefecture              VARCHAR(20),
    contact_prefecture_kana         VARCHAR(100),
    contact_city                    VARCHAR(100),
    contact_city_kana               VARCHAR(200),
    contact_town                    VARCHAR(100),
    contact_town_kana               VARCHAR(200),
    contact_street_number           VARCHAR(100),
    contact_street_number_kana      VARCHAR(200),
    contact_building                VARCHAR(200),
    contact_building_kana           VARCHAR(200),
    contact_department              VARCHAR(100),
    contact_email                   VARCHAR(255),
    contact_phone1                  VARCHAR(20),
    contact_phone2                  VARCHAR(20),

    -- STEP 5: 口座情報
    bank_code                       VARCHAR(4),
    bank_name                       VARCHAR(100),
    branch_code                     VARCHAR(3),
    branch_name                     VARCHAR(100),
    -- account_type: 普通 / 当座
    account_type                    VARCHAR(5),
    account_number                  VARCHAR(7),
    account_holder_kana             VARCHAR(200),

    -- STEP 6: 店舗情報（店舗名・ブランド名）
    store_name                      VARCHAR(64),
    store_name_kana                 VARCHAR(128),
    store_name_en                   VARCHAR(200),
    store_brand_name                VARCHAR(200),
    store_brand_name_kana           VARCHAR(400),
    store_brand_name_en             VARCHAR(400),
    -- STEP 6: 店舗業種・商材
    store_industry_category         VARCHAR(100),
    store_industry_detail           VARCHAR(100),
    store_product_description       VARCHAR(500),
    store_count                     INTEGER,
    store_average_price             INTEGER,
    -- STEP 6: 口座・レシート
    store_bank_account              VARCHAR(200),
    store_receipt_name              VARCHAR(100),
    -- STEP 6: 地図掲載
    map_display_desired             BOOLEAN       NOT NULL DEFAULT FALSE,
    map_display_desired_date        DATE,
    store_latitude                  VARCHAR(20),
    store_longitude                 VARCHAR(20),
    -- STEP 6: 営業時間・定休日
    business_hours1_start           TIME,
    business_hours1_end             TIME,
    business_hours2_start           TIME,
    business_hours2_end             TIME,
    closed_monday                   BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_tuesday                  BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_wednesday                BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_thursday                 BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_friday                   BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_saturday                 BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_sunday                   BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_holiday                  BOOLEAN       NOT NULL DEFAULT FALSE,
    closed_holiday_eve              BOOLEAN       NOT NULL DEFAULT FALSE,
    -- STEP 6: 店舗所在地・連絡先
    shop_zip_code                   VARCHAR(8),
    shop_prefecture                 VARCHAR(20),
    shop_prefecture_kana            VARCHAR(100),
    shop_city                       VARCHAR(100),
    shop_city_kana                  VARCHAR(200),
    shop_town                       VARCHAR(100),
    shop_town_kana                  VARCHAR(200),
    shop_street_number              VARCHAR(100),
    shop_street_number_kana         VARCHAR(200),
    shop_building                   VARCHAR(200),
    shop_building_kana              VARCHAR(200),
    shop_phone                      VARCHAR(20),
    shop_business_permit_number     VARCHAR(100),
    -- STEP 6: 端末情報。terminal_ic_status: IC対応済み / IC非対応 / IC対応予定あり
    terminal_ic_status              VARCHAR(30),
    -- terminal_possession_status: 保持中 / 保持していない / 非保持化予定
    terminal_possession_status      VARCHAR(30),

    -- STEP 8: 発送申込（mPOS端末）
    mpos_quantity                   INTEGER,
    delivery_zip_code               VARCHAR(8),
    delivery_prefecture             VARCHAR(20),
    delivery_prefecture_kana        VARCHAR(100),
    delivery_city                   VARCHAR(100),
    delivery_city_kana              VARCHAR(200),
    delivery_town                   VARCHAR(100),
    delivery_town_kana              VARCHAR(200),
    delivery_street_number          VARCHAR(100),
    delivery_street_number_kana     VARCHAR(200),
    delivery_building               VARCHAR(200),
    delivery_building_kana          VARCHAR(200),
    delivery_phone                  VARCHAR(20),
    delivery_receiver               VARCHAR(200),

    -- 本パスワード登録
    temp_password_hash              VARCHAR(100),
    password_hash                   VARCHAR(100),
    password_set_flg                BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_m_merchant_application PRIMARY KEY (member_code)
);

ALTER TABLE m_merchant_application OWNER TO dapaycore;

ALTER TABLE m_merchant_application
    ADD COLUMN IF NOT EXISTS delete_flag BOOLEAN NOT NULL DEFAULT FALSE;
