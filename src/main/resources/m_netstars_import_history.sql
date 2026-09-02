-- ネットスターズ還元データ取込履歴。
-- 日次スケジューラによる取込の 1 回の実行につき 1 レコードを記録する。
-- 受信はアプリケーション（スケジューラ）が管理し、この履歴を画面で確認する。
CREATE TABLE IF NOT EXISTS m_netstars_import_history (
    id               BIGSERIAL     NOT NULL,
    -- import_type: 現在は SCHEDULED（日次スケジューラ）のみ。将来の区分追加に備えた列。
    import_type      VARCHAR(20)   NOT NULL,
    -- status: RUNNING / SUCCESS / FAILED / SKIPPED（API未設定などで接続せず終了）
    status          VARCHAR(20)   NOT NULL,
    begin_date      DATE          NOT NULL,
    end_date        DATE          NOT NULL,
    fetched_count   INTEGER       NOT NULL DEFAULT 0,
    inserted_count  INTEGER       NOT NULL DEFAULT 0,
    updated_count   INTEGER       NOT NULL DEFAULT 0,
    page_count      INTEGER       NOT NULL DEFAULT 0,
    error_message   TEXT,
    -- executed_by: 実行者の識別子を保持する予備列。スケジューラ実行では NULL。
    executed_by     VARCHAR(20),
    started_at      TIMESTAMP     NOT NULL,
    finished_at     TIMESTAMP,

    CONSTRAINT pk_m_netstars_import_history PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_m_netstars_import_history_started
    ON m_netstars_import_history (started_at);

CREATE INDEX IF NOT EXISTS ix_m_netstars_import_history_status_date
    ON m_netstars_import_history (status, end_date);

ALTER TABLE m_netstars_import_history OWNER TO dapaycore;
