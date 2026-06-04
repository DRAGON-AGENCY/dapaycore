-- 社員マスタの初期データ。既に存在する場合は何もしない（冪等）。
-- password 列は BCrypt ハッシュ（平文「password」のハッシュ例）を格納する。
INSERT INTO m_employee (
    user_id, email, password, password_error_count,
    employee_name, employee_name_kana, department, authority_code,
    phone_number, fax_number, created_at, updated_at, update_user_id)
VALUES
    ('user001', 'sato.kenichi@example.co.jp',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0,
     '佐藤 健一', 'サトウ ケンイチ', '営業部', '01',
     '03-1234-5678', '03-1234-5679', NOW(), NOW(), 'user001'),
    ('user002', 'aoki.misaki@example.co.jp',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0,
     '青木 美咲', 'アオキ ミサキ', '管理部', '02',
     '052-222-3333', NULL, NOW(), NOW(), 'user001'),
    ('user003', 'takeda.satoru@example.co.jp',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0,
     '武田 智', 'タケダ サトル', 'システム部', '02',
     '06-4444-5555', '06-4444-5556', NOW(), NOW(), 'user001'),
    ('user004', 'nakagawa.yuko@example.co.jp',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0,
     '中川 優子', 'ナカガワ ユウコ', '営業部', '03',
     '03-7777-8888', NULL, NOW(), NOW(), 'user001'),
    ('user005', 'hayashi.daichi@example.co.jp',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0,
     '林 大地', 'ハヤシ ダイチ', 'カスタマーサポート', '02',
     '011-999-0000', '011-999-0001', NOW(), NOW(), 'user001')
ON CONFLICT (user_id) DO NOTHING;
