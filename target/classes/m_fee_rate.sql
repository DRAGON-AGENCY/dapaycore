-- 手数料レートマスタ。会員コード・決済手段ごとに手数料率と適用期間を管理する。
CREATE TABLE IF NOT EXISTS m_fee_rate (
    id              BIGSERIAL       NOT NULL,
    member_code     VARCHAR(20)     NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE,
    fee_rate        NUMERIC(6, 4)   NOT NULL,
    delete_flag     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_m_fee_rate PRIMARY KEY (id),
    CONSTRAINT fk_fee_rate_member
        FOREIGN KEY (member_code) REFERENCES m_merchant_application (member_code)
);

CREATE INDEX IF NOT EXISTS ix_m_fee_rate_member
    ON m_fee_rate (member_code);

ALTER TABLE m_fee_rate OWNER TO dapaycore;
