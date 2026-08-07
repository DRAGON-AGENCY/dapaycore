-- 振込手数料マスタ。振込先銀行コードごとの振込手数料を管理する。
-- bank_code = 'DEFAULT' の行は、該当する銀行コードの行が無い場合に使用する既定値。
CREATE TABLE IF NOT EXISTS m_transfer_fee (
    bank_code       VARCHAR(20)     NOT NULL,
    transfer_fee    INTEGER         NOT NULL,
    remarks         VARCHAR(500),
    update_user_id  VARCHAR(20),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_flag     BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_m_transfer_fee PRIMARY KEY (bank_code)
);

ALTER TABLE m_transfer_fee OWNER TO dapaycore;

-- 既定値の行。振込先銀行コードに該当する行が無い場合のフォールバックとして使用する。
-- 実際の振込手数料額は運用担当者が画面から更新すること。
INSERT INTO m_transfer_fee (bank_code, transfer_fee, remarks)
VALUES ('DEFAULT', 0, '既定の振込手数料（該当する銀行コードの行が無い場合に使用）。実際の金額に更新してください。')
ON CONFLICT (bank_code) DO NOTHING;
