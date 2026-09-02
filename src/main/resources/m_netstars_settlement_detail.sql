-- ネットスターズ還元データ（取引明細）。
-- StarPay 還元データ項目仕様書 v1.0.11 の CSV 1 行が 1 レコードに対応する。
-- 支払（PAY）・返金（REFUND）・取消（REVOKED）の取引情報を保持する。
-- 仮確定データのため、日次の再取得ウィンドウ内で dedup_key により UPSERT される。
CREATE TABLE IF NOT EXISTS m_netstars_settlement_detail (
    id                 BIGSERIAL      NOT NULL,

    -- 再取得時の重複排除キー。
    -- サーバー取引番号（mch_trade_no）が非空ならそれを使用し、
    -- 空（取消など）の場合は out_trade_no + '|' + trade_type + '|' + trade_time_raw を使用する。
    dedup_key          VARCHAR(120)   NOT NULL,

    shop_code          VARCHAR(32)    NOT NULL,
    shop_name          VARCHAR(128),
    -- 取引時間の生値（yyyyMMddHHmmss）。仕様外の桁数で届く場合があるため原文を保持する。
    trade_time_raw     VARCHAR(20)    NOT NULL,
    -- trade_time_raw を解釈できた場合の取引時間。解釈できない場合は NULL。
    trade_time         TIMESTAMP,
    mch_trade_no       VARCHAR(32)    NOT NULL DEFAULT '',
    dev_trade_no       VARCHAR(20)    NOT NULL DEFAULT '',
    -- trade_type: PAY（支払） / REFUND（返金） / REVOKED（取消）
    trade_type         VARCHAR(8)     NOT NULL,
    pay_type           VARCHAR(32)    NOT NULL DEFAULT '',
    -- 取引金額（円・小数点なし）。Int32 の範囲だが余裕を持って BIGINT とする。
    amount             BIGINT         NOT NULL DEFAULT 0,
    currency           VARCHAR(3)     NOT NULL DEFAULT 'JPY',
    device_id          VARCHAR(96)    NOT NULL DEFAULT '',
    device_no          VARCHAR(8)     NOT NULL DEFAULT '',
    -- Mch 取引番号。支払と返金で同一値になり、PAY / REFUND の紐付けに使用する。
    out_trade_no       VARCHAR(32)    NOT NULL,
    detail             VARCHAR(256)   NOT NULL DEFAULT '',
    attach             VARCHAR(128)   NOT NULL DEFAULT '',

    -- 最後にこの行を取り込んだ取込履歴の id。
    import_history_id  BIGINT,
    first_imported_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_imported_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_m_netstars_settlement_detail PRIMARY KEY (id),
    CONSTRAINT uq_m_netstars_settlement_detail_dedup UNIQUE (dedup_key)
);

CREATE INDEX IF NOT EXISTS ix_m_netstars_settlement_detail_out_trade
    ON m_netstars_settlement_detail (out_trade_no);

CREATE INDEX IF NOT EXISTS ix_m_netstars_settlement_detail_shop_time
    ON m_netstars_settlement_detail (shop_code, trade_time);

CREATE INDEX IF NOT EXISTS ix_m_netstars_settlement_detail_trade_time
    ON m_netstars_settlement_detail (trade_time);

ALTER TABLE m_netstars_settlement_detail OWNER TO dapaycore;
