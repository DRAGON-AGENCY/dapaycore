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

ALTER TABLE m_employee OWNER TO dapaycore;

ALTER TABLE m_employee ADD COLUMN IF NOT EXISTS delete_flag BOOLEAN NOT NULL DEFAULT FALSE;

-- 社員番号の採番に使用するシーケンス。
-- 同時登録によるレースコンディションを防ぐため、アプリ側ではなく DB 側で採番する。
CREATE SEQUENCE IF NOT EXISTS seq_employee_number;

ALTER SEQUENCE seq_employee_number OWNER TO dapaycore;

-- 既存データがある場合はシーケンスをその最大値で初期化する。
-- 何度実行しても安全 (冪等)。
DO $$
DECLARE
    current_max BIGINT;
BEGIN
    SELECT COALESCE(
        MAX(CAST(regexp_replace(employee_number, '\D+', '', 'g') AS BIGINT)), 0
    )
    INTO current_max
    FROM m_employee
    WHERE employee_number ~ '^[a-z]+[0-9]+$';

    IF current_max > 0 THEN
        PERFORM setval('seq_employee_number', current_max, true);
    END IF;
END $$;
