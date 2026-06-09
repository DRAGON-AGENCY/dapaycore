-- 社員マスタ。社員管理画面の一覧・登録・更新・削除で使用する。
CREATE TABLE IF NOT EXISTS m_employee (
    employee_number      VARCHAR(20)  NOT NULL,
    email                VARCHAR(255) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    password_error_count INTEGER      NOT NULL DEFAULT 0,
    employee_name        VARCHAR(100) NOT NULL,
    employee_name_kana   VARCHAR(100) NOT NULL,
    department           VARCHAR(100) NOT NULL,
    authority_code       VARCHAR(2)   NOT NULL,
    phone_number         VARCHAR(20)  NOT NULL,
    fax_number           VARCHAR(20),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    update_user_id       VARCHAR(20),
    CONSTRAINT pk_m_employee PRIMARY KEY (employee_number)
);

-- メールアドレスは社員を識別する情報のため一意とする。
CREATE UNIQUE INDEX IF NOT EXISTS ux_m_employee_email ON m_employee (email);

-- お問い合わせ。加盟店からのお問い合わせ内容を管理する。
CREATE TABLE IF NOT EXISTS m_contact_inquiry (
    inquiry_number   VARCHAR(20)  NOT NULL,
    category         VARCHAR(100) NOT NULL,
    subject          VARCHAR(100) NOT NULL,
    body             TEXT         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'RECEIVED',
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_m_contact_inquiry PRIMARY KEY (inquiry_number)
);
