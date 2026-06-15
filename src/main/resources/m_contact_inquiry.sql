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

ALTER TABLE m_contact_inquiry OWNER TO dapaycore;
