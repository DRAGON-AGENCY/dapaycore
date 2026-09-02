-- ネットスターズ還元データ日次取込の稼働設定。1 行のみ（config_key = 'DAILY_IMPORT'）。
-- トラブル時に画面から STOP / RESTART でき、STOP から一定日数が経過すると
-- スケジューラが自動で再開する。
CREATE TABLE IF NOT EXISTS m_netstars_import_config (
    config_key       VARCHAR(50)  NOT NULL,
    -- enabled = false の間、日次取込は実行されない（履歴に「停止中」と記録）。
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    -- enabled を false にした日時。STOP からの経過日数（自動再開判定）に使用する。
    stopped_at       TIMESTAMP,
    -- 直近の再開が自動（5 日経過による）だったかどうか。画面表示用。
    auto_resumed     BOOLEAN      NOT NULL DEFAULT FALSE,
    note             VARCHAR(500),
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id   VARCHAR(20),

    CONSTRAINT pk_m_netstars_import_config PRIMARY KEY (config_key)
);

ALTER TABLE m_netstars_import_config OWNER TO dapaycore;

INSERT INTO m_netstars_import_config (config_key, enabled, note)
VALUES ('DAILY_IMPORT', TRUE, '還元データ日次取込の稼働フラグ')
ON CONFLICT (config_key) DO NOTHING;
