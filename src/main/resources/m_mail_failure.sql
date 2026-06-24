-- 登録完了メール送信の永続的な失敗を記録するテーブル。
-- 3 回リトライしても届かなかった場合に挿入される。管理者が定期的に確認し対処する。
CREATE TABLE IF NOT EXISTS m_mail_failure (
    id              BIGSERIAL      NOT NULL,
    member_code     VARCHAR(20)    NOT NULL,
    to_address      VARCHAR(255)   NOT NULL,
    corporate_name  VARCHAR(200),
    retry_count     INT            NOT NULL DEFAULT 0,
    last_error      TEXT,
    failed_at       TIMESTAMP      NOT NULL,
    resolved        BOOLEAN        NOT NULL DEFAULT FALSE,
    resolved_at     TIMESTAMP,

    CONSTRAINT pk_m_mail_failure PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_m_mail_failure_resolved
    ON m_mail_failure (resolved, failed_at);

ALTER TABLE m_mail_failure OWNER TO dapaycore;
