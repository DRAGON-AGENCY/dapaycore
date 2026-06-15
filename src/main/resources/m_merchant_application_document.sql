-- 加盟店申込 添付書類。申込1件に対して複数のファイルを管理する。
-- document_type: BUSINESS_PERMIT（営業許可関連書類）
--              / ID_FRONT（本人確認書類・表面）
--              / ID_BACK（本人確認書類・裏面）
--              / OPENING_PLAN（開業届・店舗図面・設計書）
--              / PRODUCT_MATERIAL（商材がわかる資料）
--              / EVENT_VENUE（イベント会場図面）
--              / STORE_PHOTO（店舗外観・内観写真）
CREATE TABLE IF NOT EXISTS m_merchant_application_document (
    document_id        BIGSERIAL     NOT NULL,
    member_code VARCHAR(20)   NOT NULL,
    document_type      VARCHAR(50)   NOT NULL,
    file_name          VARCHAR(500)  NOT NULL,
    file_path          VARCHAR(1000) NOT NULL,
    file_size          BIGINT,
    uploaded_at        TIMESTAMP     NOT NULL,

    CONSTRAINT pk_m_merchant_application_document
        PRIMARY KEY (document_id),
    CONSTRAINT fk_merchant_document_application
        FOREIGN KEY (member_code)
        REFERENCES m_merchant_application (member_code)
);

CREATE INDEX IF NOT EXISTS ix_m_merchant_application_document_app
    ON m_merchant_application_document (member_code);

ALTER TABLE m_merchant_application_document OWNER TO dapaycore;
