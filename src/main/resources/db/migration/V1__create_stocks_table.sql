CREATE TABLE IF NOT EXISTS stocks (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(16)     NOT NULL UNIQUE,
    company_name    VARCHAR(255)    NOT NULL,
    sector          VARCHAR(100)    NOT NULL,
    exchange        VARCHAR(50)     NOT NULL,
    currency        VARCHAR(10)     NOT NULL DEFAULT 'USD',
    base_price      NUMERIC(18,4)   NOT NULL,
    last_price      NUMERIC(18,4)   NOT NULL,
    day_open        NUMERIC(18,4)   NOT NULL,
    day_high        NUMERIC(18,4)   NOT NULL,
    day_low         NUMERIC(18,4)   NOT NULL,
    day_volume      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_stocks_sector ON stocks (sector);
CREATE INDEX IF NOT EXISTS idx_stocks_exchange ON stocks (exchange);
