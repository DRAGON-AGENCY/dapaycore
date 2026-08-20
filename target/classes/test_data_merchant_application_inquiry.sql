-- 申込内容照会画面 画面操作テスト用データ投入スクリプト
-- m_merchant_application の会員コードは m_sequence（seq_name='member_code', seq_year=2026）で
-- 採番されており、2026-08-12 時点の実データは MA-2026-00001〜00006 まで使用済み。
-- 衝突を避けるため本スクリプトは MA-2026-00007〜00011 を使用し、末尾で m_sequence を
-- 11 まで進める（以降にアプリ画面から新規申込した場合は MA-2026-00012 から採番される）。
--
-- 内訳:
--   MA-2026-00007: UNREVIEWED（未審査）、全項目入力済み、書類7種類すべて提出済み
--   MA-2026-00008: REVIEWING（審査中）、全項目入力済み、書類は未提出（0件）
--   MA-2026-00009: APPROVED（承認済み）、全項目入力済み、書類2種類のみ提出済み
--   MA-2026-00010: REJECTED（否認）、全項目入力済み、書類3種類のみ提出済み
--   MA-2026-00011: 論理削除済み（delete_flag=TRUE）、会員一覧・照会画面に表示されないことの確認用
--
-- 実行方法: pgAdmin のクエリツール、または psql でこのファイルを実行する。
-- 冪等実行: m_merchant_application は member_code（PK）に ON CONFLICT DO NOTHING を付与。
--           m_merchant_application_document は再実行時の重複を避けるため、対象の
--           member_code の書類を一度 DELETE してから INSERT する。

-- =========================================================
-- 1. 申込情報 5件
-- =========================================================
INSERT INTO m_merchant_application (
    member_code, application_status, submitted_at, created_at, updated_at, update_user_id,
    agreed_starpay, agreed_jcb, agreed_ryugin_visa_mc_cu, agreed_ryugin_cu_qr,
    agreed_agency_delegation, agreed_service_terms, agreed_privacy_policy, agreed_authority_confirmed,
    tx_type_visit_sales, tx_type_continuous_service, tx_type_phone_solicitation, tx_type_prepaid_service,
    tx_type_business_induction, tx_type_chain_sales, tx_type_none_applicable,
    business_entity_type, sales_format, operation_format,
    pay_qr_paypay, pay_qr_d_barai, pay_qr_rakuten_pay, pay_qr_alipay_plus,
    pay_qr_wechat_pay, pay_qr_au_pay, pay_qr_merpay, pay_qr_jkopay,
    pay_credit_jcb, pay_credit_visa, pay_credit_mastercard, pay_credit_discover, pay_credit_diners, pay_credit_amex,
    pay_credit_bonus, pay_credit_two_times, pay_credit_installment, pay_credit_revolving,
    pay_emoney_id, pay_emoney_waon, pay_emoney_rakuten_edy, pay_emoney_nanaco, pay_emoney_transit_ic,
    pay_emoney_quick_pay, pay_emoney_apple_pay,
    corporate_number, corporate_name, corporate_name_kana, corporate_name_en, establishment_date,
    corporate_type, brand_name, brand_name_kana, brand_name_en, company_url,
    annual_revenue, capital_amount, employee_count, industry_category, industry_detail, business_description,
    company_zip_code, company_prefecture, company_prefecture_kana, company_city, company_city_kana,
    company_town, company_town_kana, company_street_number, company_street_number_kana,
    company_building, company_building_kana, company_phone, company_fax, company_mobile,
    rep_last_name, rep_last_name_kana, rep_last_name_en, rep_first_name, rep_first_name_kana, rep_first_name_en,
    rep_birth_date, rep_gender, rep_zip_code, rep_prefecture, rep_prefecture_kana, rep_city, rep_city_kana,
    rep_town, rep_town_kana, rep_street_number, rep_street_number_kana, rep_building, rep_phone,
    contact_last_name, contact_last_name_kana, contact_first_name, contact_first_name_kana,
    contact_zip_code, contact_prefecture, contact_prefecture_kana, contact_city, contact_city_kana,
    contact_town, contact_town_kana, contact_street_number, contact_street_number_kana,
    contact_building, contact_building_kana, contact_department, contact_email, contact_phone1, contact_phone2,
    bank_code, bank_name, branch_code, branch_name, account_type, account_number, account_holder_kana,
    store_name, store_name_kana, store_name_en, store_brand_name, store_brand_name_kana, store_brand_name_en,
    store_industry_category, store_industry_detail, store_product_description, store_count, store_average_price,
    store_bank_account, store_receipt_name,
    map_display_desired, business_hours1_start, business_hours1_end,
    closed_monday, closed_tuesday, closed_wednesday, closed_thursday, closed_friday, closed_saturday, closed_sunday,
    closed_holiday, closed_holiday_eve,
    shop_zip_code, shop_prefecture, shop_prefecture_kana, shop_city, shop_city_kana, shop_town, shop_town_kana,
    shop_street_number, shop_street_number_kana, shop_building, shop_building_kana, shop_phone,
    terminal_possession_status, terminal_ic_status,
    mpos_quantity, delivery_zip_code, delivery_prefecture, delivery_prefecture_kana, delivery_city, delivery_city_kana,
    delivery_town, delivery_town_kana, delivery_street_number, delivery_street_number_kana,
    delivery_building, delivery_building_kana, delivery_phone, delivery_receiver
)
VALUES
-- MA-2026-00007: 未審査・全項目入力済み・書類すべて提出済み
(
    'MA-2026-00007', 'UNREVIEWED', '2026-08-01 10:00:00', NOW(), NOW(), NULL,
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '法人', '固定店舗', '個店',
    TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    TRUE, TRUE, TRUE, FALSE, FALSE, FALSE,
    'する', 'する', 'する', 'しない',
    TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, TRUE,
    '1234567890123', '株式会社カブキマート', 'カブシキガイシャカブキマート', 'Kabuki Mart Inc.', '2005-04-01',
    '株式会社', 'カブキマート', 'カブキマート', 'Kabuki Mart', 'https://kabuki-mart.example.co.jp',
    250000000, 30000000, 25, '小売業', '衣料品・雑貨店', '衣料品・雑貨の店舗販売',
    '1050001', '東京都', 'トウキョウト', '港区', 'ミナトク',
    '虎ノ門', 'トラノモン', '1-2-3', '1-2-3',
    '虎ノ門タワー5F', 'トラノモンタワー5F', '0355551001', '0355551002', '09011112222',
    '佐々木', 'ササキ', 'Sasaki', '健', 'ケン', 'Ken',
    '1975-06-15', '男', '1050001', '東京都', 'トウキョウト', '港区', 'ミナトク',
    '虎ノ門', 'トラノモン', '1-2-3', '1-2-3', 'サンプルマンション101', '0355551001',
    '田村', 'タムラ', '由美', 'ユミ',
    '1050001', '東京都', 'トウキョウト', '港区', 'ミナトク',
    '虎ノ門', 'トラノモン', '1-2-3', '1-2-3',
    '虎ノ門タワー5F', 'トラノモンタワー5F', '経営企画部', 'tamura@kabuki-mart.example.co.jp', '0355551003', '0355551004',
    '0001', 'みずほ銀行', '001', '本店', '普通', '1234567', 'カブシキガイシャカブキマート',
    'カブキマート虎ノ門店', 'カブキマートトラノモンテン', 'Kabuki Mart Toranomon', 'カブキマート', 'カブキマート', 'Kabuki Mart',
    '小売業', '衣料品店', '紳士婦人衣料品・雑貨の販売', 3, 4500,
    'みずほ銀行本店 普通1234567', 'カブキマート',
    TRUE, '10:00', '20:00',
    FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '1050001', '東京都', 'トウキョウト', '港区', 'ミナトク', '虎ノ門', 'トラノモン',
    '1-2-3', '1-2-3', '虎ノ門タワー1F', 'トラノモンタワー1F', '0355551005',
    '保持中', 'IC対応済み',
    3, '1050001', '東京都', 'トウキョウト', '港区', 'ミナトク',
    '虎ノ門', 'トラノモン', '1-2-3', '1-2-3',
    '虎ノ門タワー1F', 'トラノモンタワー1F', '0355551005', '佐々木 健'
),
-- MA-2026-00008: 審査中・全項目入力済み・書類は未提出
(
    'MA-2026-00008', 'REVIEWING', '2026-08-03 11:30:00', NOW(), NOW(), NULL,
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '法人', '固定店舗', '商業施設',
    TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE,
    TRUE, TRUE, FALSE, FALSE, FALSE, FALSE,
    'しない', 'する', 'しない', 'しない',
    FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE,
    '2234567890123', '合同会社サンライズカフェ', 'ゴウドウガイシャサンライズカフェ', 'Sunrise Cafe LLC', '2012-09-15',
    '合同会社', 'サンライズカフェ', 'サンライズカフェ', 'Sunrise Cafe', 'https://sunrise-cafe.example.co.jp',
    80000000, 5000000, 12, '飲食業', 'カフェ・喫茶店', '喫茶・軽食の提供',
    '5300001', '大阪府', 'オオサカフ', '大阪市北区', 'オオサカシキタク',
    '梅田', 'ウメダ', '3-4-5', '3-4-5',
    '梅田グランドビル2F', 'ウメダグランドビル2F', '0663332001', '0663332002', '08022223333',
    '山本', 'ヤマモト', 'Yamamoto', '直子', 'ナオコ', 'Naoko',
    '1982-11-03', '女', '5300001', '大阪府', 'オオサカフ', '大阪市北区', 'オオサカシキタク',
    '梅田', 'ウメダ', '3-4-5', '3-4-5', 'サンライズマンション202', '0663332001',
    '中村', 'ナカムラ', '拓也', 'タクヤ',
    '5300001', '大阪府', 'オオサカフ', '大阪市北区', 'オオサカシキタク',
    '梅田', 'ウメダ', '3-4-5', '3-4-5',
    '梅田グランドビル2F', 'ウメダグランドビル2F', '店舗管理部', 'nakamura@sunrise-cafe.example.co.jp', '0663332003', '0663332004',
    '0009', '三井住友銀行', '100', '梅田支店', '普通', '2345678', 'ゴウドウガイシャサンライズカフェ',
    'サンライズカフェ梅田店', 'サンライズカフェウメダテン', 'Sunrise Cafe Umeda', 'サンライズカフェ', 'サンライズカフェ', 'Sunrise Cafe',
    '飲食業', 'カフェ', 'コーヒー・軽食の提供', 1, 900,
    '三井住友銀行梅田支店 普通2345678', 'サンライズカフェ',
    TRUE, '08:00', '21:00',
    FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '5300001', '大阪府', 'オオサカフ', '大阪市北区', 'オオサカシキタク', '梅田', 'ウメダ',
    '3-4-5', '3-4-5', '梅田グランドビル1F', 'ウメダグランドビル1F', '0663332005',
    '保持していない', 'IC対応予定あり',
    1, '5300001', '大阪府', 'オオサカフ', '大阪市北区', 'オオサカシキタク',
    '梅田', 'ウメダ', '3-4-5', '3-4-5',
    '梅田グランドビル1F', 'ウメダグランドビル1F', '0663332005', '山本 直子'
),
-- MA-2026-00009: 承認済み・全項目入力済み・書類2種類のみ提出済み
(
    'MA-2026-00009', 'APPROVED', '2026-07-20 09:00:00', NOW(), NOW(), 'user001',
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '個人事業主', '固定店舗', '個店',
    FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE,
    TRUE, FALSE, FALSE, FALSE, FALSE, FALSE,
    'しない', 'しない', 'しない', 'しない',
    TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, TRUE,
    '3234567890123', '有限会社みどり書店', 'ユウゲンガイシャミドリショテン', 'Midori Books Ltd.', '1998-03-20',
    '有限会社', 'みどり書店', 'ミドリショテン', 'Midori Books', 'https://midori-books.example.co.jp',
    45000000, 3000000, 6, '小売業', '書店', '書籍・文房具の販売',
    '8100001', '福岡県', 'フクオカケン', '福岡市中央区', 'フクオカシチュウオウク',
    '天神', 'テンジン', '2-1-8', '2-1-8',
    NULL, NULL, '0921112001', NULL, '09033334444',
    '小林', 'コバヤシ', 'Kobayashi', '誠', 'マコト', 'Makoto',
    '1968-02-28', '男', '8100001', '福岡県', 'フクオカケン', '福岡市中央区', 'フクオカシチュウオウク',
    '天神', 'テンジン', '2-1-8', '2-1-8', NULL, '0921112001',
    '小林', 'コバヤシ', '誠', 'マコト',
    '8100001', '福岡県', 'フクオカケン', '福岡市中央区', 'フクオカシチュウオウク',
    '天神', 'テンジン', '2-1-8', '2-1-8',
    NULL, NULL, '店主', 'kobayashi@midori-books.example.co.jp', '0921112001', NULL,
    '0017', '福岡銀行', '005', '天神支店', '普通', '3456789', 'ユウゲンガイシャミドリショテン',
    'みどり書店天神本店', 'ミドリショテンテンジンホンテン', 'Midori Books Tenjin', 'みどり書店', 'ミドリショテン', 'Midori Books',
    '小売業', '書店', '書籍・雑誌・文房具の販売', 1, 1500,
    '福岡銀行天神支店 普通3456789', 'みどり書店',
    FALSE, '09:30', '19:30',
    FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '8100001', '福岡県', 'フクオカケン', '福岡市中央区', 'フクオカシチュウオウク', '天神', 'テンジン',
    '2-1-8', '2-1-8', NULL, NULL, '0921112002',
    '保持中', 'IC非対応',
    1, '8100001', '福岡県', 'フクオカケン', '福岡市中央区', 'フクオカシチュウオウク',
    '天神', 'テンジン', '2-1-8', '2-1-8',
    NULL, NULL, '0921112002', '小林 誠'
),
-- MA-2026-00010: 否認・全項目入力済み・書類3種類のみ提出済み
(
    'MA-2026-00010', 'REJECTED', '2026-07-10 14:15:00', NOW(), NOW(), 'user002',
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE,
    '法人', '移動販売', '該当しない',
    TRUE, TRUE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE,
    FALSE, TRUE, TRUE, FALSE, FALSE, FALSE,
    'する', 'しない', 'する', 'する',
    FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '4234567890123', '株式会社ノーザンスポーツ', 'カブシキガイシャノーザンスポーツ', 'Northern Sports Inc.', '2015-05-10',
    '株式会社', 'ノーザンスポーツ', 'ノーザンスポーツ', 'Northern Sports', 'https://northern-sports.example.co.jp',
    120000000, 10000000, 18, '小売業', 'スポーツ用品店', 'スポーツ用品・アウトドア用品の販売',
    '0600001', '北海道', 'ホッカイドウ', '札幌市中央区', 'サッポロシチュウオウク',
    '北一条', 'キタイチジョウ', '4-2-1', '4-2-1',
    '札幌一条ビル3F', 'サッポロイチジョウビル3F', '0112223001', '0112223002', '09044445555',
    '高橋', 'タカハシ', 'Takahashi', '陽子', 'ヨウコ', 'Yoko',
    '1980-09-09', '女', '0600001', '北海道', 'ホッカイドウ', '札幌市中央区', 'サッポロシチュウオウク',
    '北一条', 'キタイチジョウ', '4-2-1', '4-2-1', 'ノースマンション305', '0112223001',
    '伊藤', 'イトウ', '大輔', 'ダイスケ',
    '0600001', '北海道', 'ホッカイドウ', '札幌市中央区', 'サッポロシチュウオウク',
    '北一条', 'キタイチジョウ', '4-2-1', '4-2-1',
    '札幌一条ビル3F', 'サッポロイチジョウビル3F', '営業部', 'ito@northern-sports.example.co.jp', '0112223003', '0112223004',
    '0005', '三菱UFJ銀行', '010', '札幌支店', '当座', '4567890', 'カブシキガイシャノーザンスポーツ',
    'ノーザンスポーツ札幌店', 'ノーザンスポーツサッポロテン', 'Northern Sports Sapporo', 'ノーザンスポーツ', 'ノーザンスポーツ', 'Northern Sports',
    '小売業', 'スポーツ用品店', 'スキー・スノーボード用品の販売', 2, 8000,
    '三菱UFJ銀行札幌支店 当座4567890', 'ノーザンスポーツ',
    TRUE, '10:00', '19:00',
    TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    '0600001', '北海道', 'ホッカイドウ', '札幌市中央区', 'サッポロシチュウオウク', '北一条', 'キタイチジョウ',
    '4-2-1', '4-2-1', '札幌一条ビル1F', 'サッポロイチジョウビル1F', '0112223005',
    '非保持化予定', 'IC対応済み',
    2, '0600001', '北海道', 'ホッカイドウ', '札幌市中央区', 'サッポロシチュウオウク',
    '北一条', 'キタイチジョウ', '4-2-1', '4-2-1',
    '札幌一条ビル1F', 'サッポロイチジョウビル1F', '0112223005', '高橋 陽子'
)
ON CONFLICT (member_code) DO NOTHING;

-- =========================================================
-- 2. 論理削除済みの申込情報 1件
--    会員一覧・照会画面に表示されないことを確認するためのデータ。
-- =========================================================
INSERT INTO m_merchant_application (
    member_code, application_status, created_at, updated_at,
    corporate_name, corporate_name_kana, delete_flag
)
VALUES (
    'MA-2026-00011', 'UNREVIEWED', NOW(), NOW(),
    '株式会社テストデリート', 'カブシキガイシャテストデリート', TRUE
)
ON CONFLICT (member_code) DO NOTHING;

-- =========================================================
-- 3. 添付書類（画面操作テスト用データ投入時点のみ再作成）
--    file_path はテスト用のダミーパスであり、実ファイルはディスクに存在しない。
--    そのためダウンロードリンクのクリック自体は失敗する。「提出済み／未提出」の
--    表示切り替え確認が目的で、ダウンロード動作自体の確認は別途実ファイルで行うこと。
-- =========================================================
DELETE FROM m_merchant_application_document
WHERE member_code IN ('MA-2026-00007', 'MA-2026-00008', 'MA-2026-00009', 'MA-2026-00010');

-- MA-2026-00007: 7種類すべて提出済み
INSERT INTO m_merchant_application_document
    (member_code, document_type, file_name, file_path, file_size, uploaded_at)
VALUES
    ('MA-2026-00007', 'BUSINESS_PERMIT',  'test_business_permit.pdf',  'MA-2026-00007/BUSINESS_PERMIT/test_business_permit.pdf',  102400, NOW()),
    ('MA-2026-00007', 'ID_FRONT',         'test_id_front.jpg',          'MA-2026-00007/ID_FRONT/test_id_front.jpg',                 204800, NOW()),
    ('MA-2026-00007', 'ID_BACK',          'test_id_back.jpg',           'MA-2026-00007/ID_BACK/test_id_back.jpg',                   204800, NOW()),
    ('MA-2026-00007', 'OPENING_PLAN',     'test_opening_plan.pdf',      'MA-2026-00007/OPENING_PLAN/test_opening_plan.pdf',         307200, NOW()),
    ('MA-2026-00007', 'PRODUCT_MATERIAL', 'test_product_material.pdf',  'MA-2026-00007/PRODUCT_MATERIAL/test_product_material.pdf', 307200, NOW()),
    ('MA-2026-00007', 'EVENT_VENUE',      'test_event_venue.pdf',       'MA-2026-00007/EVENT_VENUE/test_event_venue.pdf',           307200, NOW()),
    ('MA-2026-00007', 'STORE_PHOTO',      'test_store_photo.jpg',       'MA-2026-00007/STORE_PHOTO/test_store_photo.jpg',           409600, NOW());

-- MA-2026-00008: 未提出（0件、DELETEのみで意図的に何も登録しない）

-- MA-2026-00009: BUSINESS_PERMIT・ID_FRONTのみ提出済み
INSERT INTO m_merchant_application_document
    (member_code, document_type, file_name, file_path, file_size, uploaded_at)
VALUES
    ('MA-2026-00009', 'BUSINESS_PERMIT', 'test_business_permit.pdf', 'MA-2026-00009/BUSINESS_PERMIT/test_business_permit.pdf', 102400, NOW()),
    ('MA-2026-00009', 'ID_FRONT',        'test_id_front.jpg',        'MA-2026-00009/ID_FRONT/test_id_front.jpg',                204800, NOW());

-- MA-2026-00010: ID_FRONT・ID_BACK・STORE_PHOTOのみ提出済み
INSERT INTO m_merchant_application_document
    (member_code, document_type, file_name, file_path, file_size, uploaded_at)
VALUES
    ('MA-2026-00010', 'ID_FRONT',    'test_id_front.jpg',   'MA-2026-00010/ID_FRONT/test_id_front.jpg',   204800, NOW()),
    ('MA-2026-00010', 'ID_BACK',     'test_id_back.jpg',    'MA-2026-00010/ID_BACK/test_id_back.jpg',     204800, NOW()),
    ('MA-2026-00010', 'STORE_PHOTO', 'test_store_photo.jpg','MA-2026-00010/STORE_PHOTO/test_store_photo.jpg', 409600, NOW());

-- =========================================================
-- 4. 採番シーケンスを実データに追いつかせる
--    本スクリプトで MA-2026-00007〜00011 を使用したため、次回アプリ画面からの
--    新規申込が MA-2026-00007 等と衝突しないよう last_value を進める。
-- =========================================================
INSERT INTO m_sequence (seq_name, seq_year, last_value)
VALUES ('member_code', 2026, 11)
ON CONFLICT (seq_name, seq_year)
    DO UPDATE SET last_value = GREATEST(m_sequence.last_value, EXCLUDED.last_value);
