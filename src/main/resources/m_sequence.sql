CREATE TABLE IF NOT EXISTS m_sequence (
    seq_name   VARCHAR(50) NOT NULL,
    seq_year   INTEGER     NOT NULL,
    last_value BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_m_sequence PRIMARY KEY (seq_name, seq_year)
);

ALTER TABLE m_sequence OWNER TO dapaycore;
